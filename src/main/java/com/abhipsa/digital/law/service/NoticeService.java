package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Document;
import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.DocumentRepository;
import com.abhipsa.digital.law.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository repository;
    private final CurrentUserService currentUserService;
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    // Non-admins only see notices whose linked case is assigned to them; a
    // notice with no case attached has no owner, so it stays admin-only.
    private String scopeUserId() {
        return currentUserService.isAdmin() ? null : currentUserService.getUserId();
    }

    private void assertOwnership(Notice notice) {
        if (currentUserService.isAdmin()) return;
        String myId = currentUserService.getUserId();
        String assignedId = notice.getCaseDetails() != null && notice.getCaseDetails().getAssignedUser() != null
                ? notice.getCaseDetails().getAssignedUser().getId() : null;
        if (myId == null || !myId.equals(assignedId)) {
            throw new AccessDeniedException("This notice is not linked to one of your cases");
        }
    }

    public Notice create(Notice notice) {
        requireApprovedForDispatch(notice);
        applyDefaultLimitationBegins(notice);
        return repository.save(notice);
    }

    public List<Notice> getAll() {
        String myId = scopeUserId();
        List<Notice> all = myId == null ? repository.findAll() : repository.findByCaseDetails_AssignedUser_Id(myId);
        enrichWithDocumentMetadata(all);
        return all;
    }

    public Notice getById(String id) {
        Notice notice = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notice not found"));
        assertOwnership(notice);
        enrichWithDocumentMetadata(notice);
        return notice;
    }

    public Notice update(String id, Notice notice) {

        Notice existing = getById(id);

        existing.setOfficeFileNo(notice.getOfficeFileNo());
        existing.setReferenceNo(notice.getReferenceNo());
        existing.setNoticeDate(notice.getNoticeDate());
        existing.setDispatchDate(notice.getDispatchDate());
        existing.setCaseDetails(notice.getCaseDetails());
        existing.setCourierName(notice.getCourierName());
        existing.setTrackingNumber(notice.getTrackingNumber());
        existing.setDeliveryStatus(notice.getDeliveryStatus());
        existing.setSuitStatus(notice.getSuitStatus());
        existing.setDescription(notice.getDescription());
        existing.setDocuments(notice.getDocuments());
        existing.setLimitationBegins(notice.getLimitationBegins());
        existing.setClientSide(notice.getClientSide());

        requireApprovedForDispatch(existing);
        applyDefaultLimitationBegins(existing);

        return repository.save(existing);
    }

    public Notice approve(String id, User approvedBy) {
        Notice existing = getById(id);
        existing.setApproved(true);
        existing.setApprovedBy(approvedBy);
        existing.setApprovedOn(LocalDate.now());
        return repository.save(existing);
    }

    public Notice markDelivered(String id) {
        Notice existing = getById(id);
        existing.setDeliveryStatus("DELIVERED");
        existing.setDeliveredOn(LocalDate.now());
        return repository.save(existing);
    }

    // Document/receipt storage now lives in the shared Document table
    // (DocumentService) rather than the byte[] columns below - those stay
    // on the entity but are no longer written to. Uploading again to the
    // same slot replaces the previous document (DocumentService.uploadForNotice),
    // preserving this method's original overwrite-on-reupload behavior.
    public Notice saveDocument(String id, String name, String contentType, byte[] data) {
        Notice notice = getById(id);
        documentService.uploadForNotice(notice, "document", name, contentType, data);
        enrichWithDocumentMetadata(notice);
        return notice;
    }

    public Notice saveReceipt(String id, String name, String contentType, byte[] data) {
        Notice notice = getById(id);
        documentService.uploadForNotice(notice, "receipt", name, contentType, data);
        enrichWithDocumentMetadata(notice);
        return notice;
    }

    // Populates the legacy documentName/receiptName-style fields (read by
    // the existing frontend) from the current Document rows, so callers
    // never notice storage moved to a separate table.
    private void enrichWithDocumentMetadata(Notice notice) {
        documentService.findForNoticeSlot(notice.getId(), "document").ifPresent(d -> {
            notice.setDocumentName(d.getFileName());
            notice.setDocumentContentType(d.getContentType());
            notice.setDocumentSizeBytes(d.getSizeBytes());
        });
        documentService.findForNoticeSlot(notice.getId(), "receipt").ifPresent(d -> {
            notice.setReceiptName(d.getFileName());
            notice.setReceiptContentType(d.getContentType());
            notice.setReceiptSizeBytes(d.getSizeBytes());
        });
    }

    // Batched form of the above for a whole list/page, to avoid two extra
    // queries per row.
    private void enrichWithDocumentMetadata(List<Notice> notices) {
        if (notices.isEmpty()) return;
        List<String> ids = notices.stream().map(Notice::getId).toList();
        List<Document> docs = documentRepository.findByNoticeIdIn(ids);
        Map<String, Document> bySlotKey = new HashMap<>();
        for (Document d : docs) {
            if (d.getNotice() == null || d.getSlot() == null) continue;
            bySlotKey.put(d.getNotice().getId() + "|" + d.getSlot(), d);
        }
        for (Notice n : notices) {
            Document doc = bySlotKey.get(n.getId() + "|document");
            if (doc != null) {
                n.setDocumentName(doc.getFileName());
                n.setDocumentContentType(doc.getContentType());
                n.setDocumentSizeBytes(doc.getSizeBytes());
            }
            Document receipt = bySlotKey.get(n.getId() + "|receipt");
            if (receipt != null) {
                n.setReceiptName(receipt.getFileName());
                n.setReceiptContentType(receipt.getContentType());
                n.setReceiptSizeBytes(receipt.getSizeBytes());
            }
        }
    }

    // FR-LN02: a notice must be approved before it carries dispatch details.
    private void requireApprovedForDispatch(Notice notice) {
        boolean hasDispatchInfo = notice.getDispatchDate() != null
                || (notice.getCourierName() != null && !notice.getCourierName().isBlank())
                || (notice.getTrackingNumber() != null && !notice.getTrackingNumber().isBlank());

        if (hasDispatchInfo && !notice.isApproved()) {
            throw new IllegalStateException("Notice must be approved before it can be dispatched");
        }
    }

    // FR-LN06: no applicable-law table exists yet, so the begin date defaults
    // to the notice date unless the caller supplies a more specific value.
    private void applyDefaultLimitationBegins(Notice notice) {
        if (notice.getLimitationBegins() == null) {
            notice.setLimitationBegins(notice.getNoticeDate());
        }
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public Notice findByReferenceNo(String referenceNo) {
        return repository.findByReferenceNo(referenceNo)
                .orElseThrow(() ->
                        new RuntimeException("Notice not found"));
    }

    public List<Notice> findByOfficeFileNo(String officeFileNo) {
        return repository.findByOfficeFileNoContainingIgnoreCase(officeFileNo);
    }

    public List<Notice> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<Notice> findByNoticeDate(LocalDate noticeDate) {
        return repository.findByNoticeDate(noticeDate);
    }

    public List<Notice> findByDispatchDate(LocalDate dispatchDate) {
        return repository.findByDispatchDate(dispatchDate);
    }

    public List<Notice> findByNoticeDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByNoticeDateBetween(start, end);
    }

    public List<Notice> findByDispatchDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByDispatchDateBetween(start, end);
    }

    public long countByCaseId(String caseId) {
        return repository.countByCaseDetailsId(caseId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<Notice> getAllPaged(Pageable pageable) {
        String myId = scopeUserId();
        Page<Notice> page = myId == null ? repository.findAll(pageable) : repository.findByCaseDetails_AssignedUser_Id(myId, pageable);
        enrichWithDocumentMetadata(page.getContent());
        return page;
    }

    public Page<Notice> findByOfficeFileNoPaged(String officeFileNo, Pageable pageable) {
        return repository.findByOfficeFileNoContainingIgnoreCase(officeFileNo, pageable);
    }

    public Page<Notice> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }

    public Page<Notice> findByNoticeDatePaged(LocalDate noticeDate, Pageable pageable) {
        return repository.findByNoticeDate(noticeDate, pageable);
    }

    public Page<Notice> findByDispatchDatePaged(LocalDate dispatchDate, Pageable pageable) {
        return repository.findByDispatchDate(dispatchDate, pageable);
    }

    public Page<Notice> findByNoticeDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        return repository.findByNoticeDateBetween(start, end, pageable);
    }

    public Page<Notice> findByDispatchDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        return repository.findByDispatchDateBetween(start, end, pageable);
    }
}