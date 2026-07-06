package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Notification;
import com.abhipsa.digital.law.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public Notification create(Notification notification) {
        return repository.save(notification);
    }

    public List<Notification> getAll() {
        return repository.findAll();
    }

    public Notification getById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));
    }

    public Notification update(String id, Notification notification) {

        Notification existing = getById(id);

        existing.setReferenceNo(notification.getReferenceNo());
        existing.setChannel(notification.getChannel());
        existing.setMessage(notification.getMessage());
        existing.setSentAt(notification.getSentAt());
        existing.setStatus(notification.getStatus());
        existing.setCaseDetails(notification.getCaseDetails());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
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
        return repository.findByStatusContainingIgnoreCase(status);
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
        return repository.findAll(pageable);
    }

    public Page<Notification> findByChannelPaged(String channel, Pageable pageable) {
        return repository.findByChannelContainingIgnoreCase(channel, pageable);
    }

    public Page<Notification> findByStatusPaged(String status, Pageable pageable) {
        return repository.findByStatusContainingIgnoreCase(status, pageable);
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