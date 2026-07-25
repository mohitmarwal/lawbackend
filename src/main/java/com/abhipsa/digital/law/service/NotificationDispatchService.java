package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.MobileContact;
import com.abhipsa.digital.law.entity.Notification;
import com.abhipsa.digital.law.repository.MobileContactRepository;
import com.abhipsa.digital.law.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

// Fans a case-level event (status change, hearing update, ...) out to every
// contact on file for that case, by email and WhatsApp. Every attempt is
// recorded as a Notification row regardless of outcome. Sending is a
// best-effort side effect: failures here must never break the case/hearing
// write that triggered them.
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final MobileContactRepository mobileContactRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notification.enabled:false}")
    private boolean enabled;

    @Value("${notification.email.from:no-reply@matterly.in}")
    private String emailFrom;

    @Value("${notification.whatsapp.account-sid:}")
    private String twilioAccountSid;

    @Value("${notification.whatsapp.auth-token:}")
    private String twilioAuthToken;

    @Value("${notification.whatsapp.from-number:}")
    private String twilioFromNumber;

    public void notifyCaseContacts(CaseDetails caseDetails, String subject, String message) {
        if (caseDetails == null || caseDetails.getId() == null) {
            return;
        }

        List<MobileContact> contacts = mobileContactRepository.findByCaseDetailsId(caseDetails.getId());
        for (MobileContact contact : contacts) {
            if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
                sendEmail(contact.getEmail(), subject, message, caseDetails);
            }
            if (contact.getMobile() != null && !contact.getMobile().isBlank()) {
                sendWhatsApp(contact.getMobile(), message, caseDetails);
            }
        }
    }

    private void sendEmail(String toEmail, String subject, String body, CaseDetails caseDetails) {
        Notification record = newRecord("email", toEmail, subject + ": " + body, caseDetails);
        finishSend(record, trySendEmail(toEmail, subject, body));
    }

    private void sendWhatsApp(String toMobile, String message, CaseDetails caseDetails) {
        Notification record = newRecord("whatsapp", toMobile, message, caseDetails);
        finishSend(record, trySendWhatsApp(toMobile, message));
    }

    // Manually created notification (e.g. from the "New Notification" button):
    // same dispatch path as the automatic case-event flow, just triggered
    // on demand for a caller-chosen case/channel/recipient/message.
    public Notification sendManual(String channel, String recipient, String message, CaseDetails caseDetails) {
        Notification record = newRecord(channel != null ? channel.toLowerCase() : "email", recipient, message, caseDetails);
        boolean success = "whatsapp".equalsIgnoreCase(channel)
                ? trySendWhatsApp(recipient, message)
                : trySendEmail(recipient, "Notification", message);
        finishSend(record, success);
        return record;
    }

    // Re-dispatches an existing (typically failed) notification in place —
    // updates its status/retryCount rather than creating a duplicate row.
    public Notification resend(Notification existing) {
        boolean success = "whatsapp".equalsIgnoreCase(existing.getChannel())
                ? trySendWhatsApp(existing.getRecipient(), existing.getMessage())
                : trySendEmail(existing.getRecipient(), "Notification", existing.getMessage());
        if (success) {
            markSent(existing);
        } else {
            existing.setRetryCount(existing.getRetryCount() + 1);
            markFailed(existing, "resend attempt failed");
        }
        return existing;
    }

    private void finishSend(Notification record, boolean success) {
        if (success) {
            markSent(record);
        } else {
            markFailed(record, "send failed");
        }
    }

    private boolean trySendEmail(String toEmail, String subject, String body) {
        if (!enabled) return false;
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(emailFrom);
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            return true;
        } catch (Exception e) {
            log.warn("Email notification failed for {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private boolean trySendWhatsApp(String toMobile, String message) {
        if (!enabled || twilioAccountSid.isBlank() || twilioAuthToken.isBlank() || twilioFromNumber.isBlank()) {
            return false;
        }
        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(twilioAccountSid, twilioAuthToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", "whatsapp:" + toMobile);
            form.add("From", "whatsapp:" + twilioFromNumber);
            form.add("Body", message);

            restTemplate.postForEntity(url, new HttpEntity<>(form, headers), String.class);
            return true;
        } catch (Exception e) {
            log.warn("WhatsApp notification failed for {}: {}", toMobile, e.getMessage());
            return false;
        }
    }

    private Notification newRecord(String channel, String recipient, String message, CaseDetails caseDetails) {
        Notification record = new Notification();
        record.setChannel(channel);
        record.setRecipient(recipient);
        record.setMessage(message);
        record.setCaseDetails(caseDetails);
        record.setStatus("pending");
        record.setRetryCount(0);
        return record;
    }

    private void markSent(Notification record) {
        record.setStatus("sent");
        record.setSentAt(LocalDateTime.now());
        notificationRepository.save(record);
    }

    private void markFailed(Notification record, String reason) {
        record.setStatus("failed");
        notificationRepository.save(record);
    }
}
