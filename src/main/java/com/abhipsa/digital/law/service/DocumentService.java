package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.Document;
import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Pure storage/CRUD for Document rows. Deliberately has no dependency on
// CaseDetailsService/NoticeService (which would create a circular bean
// dependency, since NoticeService depends on this service) - callers
// (DocumentController, NoticeService) are responsible for checking that
// the current user may view/act on the parent case or notice before
// calling in here, e.g. via CaseDetailsService.getById()/NoticeService.getById(),
// which already throw AccessDeniedException when not permitted.
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repository;
    private final CurrentUserService currentUserService;

    public List<Document> listForCase(String caseId) {
        return repository.findByCaseDetailsIdOrderByUploadedAtDesc(caseId);
    }

    public Document uploadForCase(CaseDetails caseDetails, String fileName, String contentType, byte[] data) {
        Document document = new Document();
        document.setCaseDetails(caseDetails);
        document.setFileName(fileName);
        document.setContentType(contentType);
        document.setSizeBytes((long) data.length);
        document.setData(data);
        document.setUploadedBy(currentUserService.getUser());
        return repository.save(document);
    }

    public List<Document> listForNotice(String noticeId) {
        return repository.findByNoticeIdOrderByUploadedAtDesc(noticeId);
    }

    // slot "document"/"receipt" replace-on-reupload (preserves Notice's
    // legacy single-slot UX); any other slot (e.g. "attachment") just
    // accumulates, since that's new multi-document capability for notices.
    public Document uploadForNotice(Notice notice, String slot, String fileName, String contentType, byte[] data) {
        if ("document".equals(slot) || "receipt".equals(slot)) {
            repository.findByNoticeIdAndSlot(notice.getId(), slot).ifPresent(repository::delete);
        }
        Document document = new Document();
        document.setNotice(notice);
        document.setSlot(slot);
        document.setFileName(fileName);
        document.setContentType(contentType);
        document.setSizeBytes((long) data.length);
        document.setData(data);
        document.setUploadedBy(currentUserService.getUser());
        return repository.save(document);
    }

    public Optional<Document> findForNoticeSlot(String noticeId, String slot) {
        return repository.findByNoticeIdAndSlot(noticeId, slot);
    }

    public Document getById(String documentId) {
        return repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public void delete(String documentId) {
        Document document = getById(documentId);
        if (!currentUserService.isAdmin()) {
            String myId = currentUserService.getUserId();
            String uploaderId = document.getUploadedBy() != null ? document.getUploadedBy().getId() : null;
            if (myId == null || !myId.equals(uploaderId)) {
                throw new AccessDeniedException("You can only delete your own uploads");
            }
        }
        repository.delete(document);
    }
}
