package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Optional<Notification> findByReferenceNo(String referenceNo);

    List<Notification> findByChannel(String channel);

    List<Notification> findByChannelContainingIgnoreCase(String channel);

    List<Notification> findByStatus(String status);

    List<Notification> findByStatusContainingIgnoreCase(String status);

    List<Notification> findByCaseDetailsId(String caseId);

    List<Notification> findByMessageContainingIgnoreCase(String message);

    List<Notification> findBySentAt(LocalDateTime sentAt);

    List<Notification> findBySentAtBetween(
            LocalDateTime start,
            LocalDateTime end);

    List<Notification> findBySentAtAfter(LocalDateTime sentAt);

    List<Notification> findBySentAtBefore(LocalDateTime sentAt);

    List<Notification> findByChannelAndStatus(
            String channel,
            String status);

    long countByStatus(String status);

    long countByChannel(String channel);

    long countByCaseDetailsId(String caseId);

    boolean existsByReferenceNo(String referenceNo);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<Notification> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<Notification> findByChannelContainingIgnoreCase(String channel, Pageable pageable);

    Page<Notification> findByStatusContainingIgnoreCase(String status, Pageable pageable);

    Page<Notification> findByCaseDetailsId(String caseId, Pageable pageable);

    Page<Notification> findByMessageContainingIgnoreCase(String message, Pageable pageable);

    Page<Notification> findBySentAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}