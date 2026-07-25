package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Notification;
import com.abhipsa.digital.law.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        return service.create(notification);
    }

    // Manual "New Notification" send: dispatches immediately by email/
    // WhatsApp rather than just logging a row. Case is required so
    // non-admins can only send against a case assigned to them.
    @PostMapping("/send")
    public Notification sendManual(@RequestBody Notification notification) {
        String caseId = notification.getCaseDetails() != null ? notification.getCaseDetails().getId() : null;
        return service.sendManual(caseId, notification.getChannel(), notification.getRecipient(), notification.getMessage());
    }

    @PostMapping("/{id}/resend")
    public Notification resend(@PathVariable String id) {
        return service.resend(id);
    }

    @GetMapping
    public List<Notification> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Notification getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Notification update(
            @PathVariable String id,
            @RequestBody Notification notification) {

        return service.update(id, notification);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PostMapping("/{id}/mark-success")
    public Notification markSuccess(@PathVariable String id) {
        return service.markSuccess(id);
    }

    @PostMapping("/{id}/mark-failed")
    public Notification markFailed(@PathVariable String id) {
        return service.markFailed(id);
    }

    @PostMapping("/{id}/retry")
    public Notification retry(@PathVariable String id) {
        return service.retry(id);
    }

    @GetMapping("/failed")
    public List<Notification> listFailed() {
        return service.listFailed();
    }

    @GetMapping("/reference/{referenceNo}")
    public Notification findByReferenceNo(
            @PathVariable String referenceNo) {

        return service.findByReferenceNo(referenceNo);
    }

    @GetMapping("/channel/{channel}")
    public List<Notification> findByChannel(
            @PathVariable String channel) {

        return service.findByChannel(channel);
    }

    @GetMapping("/status/{status}")
    public List<Notification> findByStatus(
            @PathVariable String status) {

        return service.findByStatus(status);
    }

    @GetMapping("/case/{caseId}")
    public List<Notification> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/message/{message}")
    public List<Notification> findByMessage(
            @PathVariable String message) {

        return service.findByMessage(message);
    }

    @GetMapping("/count/status/{status}")
    public long countByStatus(
            @PathVariable String status) {

        return service.countByStatus(status);
    }

    @GetMapping("/count/case/{caseId}")
    public long countByCaseId(
            @PathVariable String caseId) {

        return service.countByCaseId(caseId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/notifications/paged?page=0&size=20&sort=sentAt,desc
    // ==================================================================

    @GetMapping("/paged")
    public Page<Notification> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/channel/{channel}/paged")
    public Page<Notification> findByChannelPaged(
            @PathVariable String channel,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByChannelPaged(channel, pageable);
    }

    @GetMapping("/status/{status}/paged")
    public Page<Notification> findByStatusPaged(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByStatusPaged(status, pageable);
    }

    @GetMapping("/case/{caseId}/paged")
    public Page<Notification> findByCaseIdPaged(
            @PathVariable String caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCaseIdPaged(caseId, pageable);
    }

    @GetMapping("/message/{message}/paged")
    public Page<Notification> findByMessagePaged(
            @PathVariable String message,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByMessagePaged(message, pageable);
    }

    @GetMapping("/sent-at-range/paged")
    public Page<Notification> findBySentAtRangePaged(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,

            @PageableDefault(size = 20) Pageable pageable) {

        return service.findBySentAtBetweenPaged(start, end, pageable);
    }
}