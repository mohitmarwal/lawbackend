package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.Notification;
import com.abhipsa.digital.law.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final CurrentUserService currentUserService;
    private final CaseDetailsService caseDetailsService;
    private final NotificationDispatchService notificationDispatchService;

    // Non-admins only see notifications whose linked case is assigned to
    // them; one with no case attached has no owner, so it stays admin-only.
    private String scopeUserId() {
        return currentUserService.isAdmin() ? null : currentUserService.getUserId();
    }

    private void assertOwnership(Notification notification) {
        if (currentUserService.isAdmin()) return;
        String myId = currentUserService.getUserId();
        String assignedId = notification.getCaseDetails() != null && notification.getCaseDetails().getAssignedUser() != null
                ? notification.getCaseDetails().getAssignedUser().getId() : null;
        if (myId == null || !myId.equals(assignedId)) {
            throw new AccessDeniedException("This notification is not linked to one of your cases");
        }
    }

    public Notification create(Notification notification) {
        return repository.save(notification);
    }

    public List<Notification> getAll() {
        String myId = scopeUserId();
        return myId == null ? repository.findAll() : repository.findByCaseDetails_AssignedUser_Id(myId);
    }

    public Notification getById(String id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));
        assertOwnership(notification);
        return notification;
    }

    public Notification update(String id, Notification notification) {

        Notification existing = getById(id);

        existing.setReferenceNo(notification.getReferenceNo());
        existing.setChannel(notification.getChannel());
        existing.setMessage(notification.getMessage());
        existing.setSentAt(notification.getSentAt());
        existing.setStatus(notification.getStatus());
        existing.setRecipient(notification.getRecipient());
        if (notification.getCaseDetails() != null && notification.getCaseDetails().getId() != null) {
            // Re-resolves (and ownership-checks) the target case, so a
            // non-admin can't relink a notification to someone else's case.
            existing.setCaseDetails(caseDetailsService.getById(notification.getCaseDetails().getId()));
        }

        return repository.save(existing);
    }

    // Manually created notification (e.g. the "New Notification" button):
    // dispatched immediately via email/WhatsApp, not just logged.
    public Notification sendManual(String caseId, String channel, String recipient, String message) {
        CaseDetails caseDetails = caseDetailsService.getById(caseId); // ownership-checked
        return notificationDispatchService.sendManual(channel, recipient, message, caseDetails);
    }

    // Re-dispatches an existing (typically failed) notification in place,
    // e.g. after editing its recipient/message via update().
    public Notification resend(String id) {
        Notification existing = getById(id); // ownership-checked
        return notificationDispatchService.resend(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public Notification markSuccess(String id) {
        Notification existing = getById(id);
        existing.setStatus("sent");
        existing.setSentAt(LocalDateTime.now());
        return repository.save(existing);
    }

    public Notification markFailed(String id) {
        Notification existing = getById(id);
        existing.setStatus("failed");
        return repository.save(existing);
    }

    // FR-NE03: retried up to 3x with exponential back-off, enforced by the
    // caller/scheduler; this just advances the record's retry state.
    public Notification retry(String id) {
        Notification existing = getById(id);
        existing.setRetryCount(existing.getRetryCount() + 1);
        existing.setStatus("pending");
        return repository.save(existing);
    }

    public List<Notification> listFailed() {
        return repository.findByStatus("failed");
    }

    public Notification findByReferenceNo(String referenceNo) {
        return repository.findByReferenceNo(referenceNo)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));
    }

    public List<Notification> findByChannel(String channel) {
        return repository.findByChannelContainingIgnoreCase(channel);
    }

    public List<Notification> findByStatus(String status) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByStatusContainingIgnoreCase(status)
                : repository.findByStatusContainingIgnoreCaseAndCaseDetails_AssignedUser_Id(status, myId);
    }

    public List<Notification> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<Notification> findByMessage(String message) {
        return repository.findByMessageContainingIgnoreCase(message);
    }

    public List<Notification> findBySentAtBetween(
            LocalDateTime start,
            LocalDateTime end) {

        return repository.findBySentAtBetween(start, end);
    }

    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    public long countByCaseId(String caseId) {
        return repository.countByCaseDetailsId(caseId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<Notification> getAllPaged(Pageable pageable) {
        String myId = scopeUserId();
        return myId == null ? repository.findAll(pageable) : repository.findByCaseDetails_AssignedUser_Id(myId, pageable);
    }

    public Page<Notification> findByChannelPaged(String channel, Pageable pageable) {
        return repository.findByChannelContainingIgnoreCase(channel, pageable);
    }

    public Page<Notification> findByStatusPaged(String status, Pageable pageable) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByStatusContainingIgnoreCase(status, pageable)
                : repository.findByStatusContainingIgnoreCaseAndCaseDetails_AssignedUser_Id(status, myId, pageable);
    }

    public Page<Notification> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }

    public Page<Notification> findByMessagePaged(String message, Pageable pageable) {
        return repository.findByMessageContainingIgnoreCase(message, pageable);
    }

    public Page<Notification> findBySentAtBetweenPaged(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {

        return repository.findBySentAtBetween(start, end, pageable);
    }
}