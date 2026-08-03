package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.Document;
import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.service.CaseDetailsService;
import com.abhipsa.digital.law.service.DocumentService;
import com.abhipsa.digital.law.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// Ownership/visibility is enforced here by delegating to
// CaseDetailsService.getById()/NoticeService.getById() - both already
// throw AccessDeniedException when the current user can't see that case/
// notice, so "can view it" doubles as "can list/upload/download/delete its
// documents" (same access model already used for every other case/notice
// sub-resource in this app).
@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final CaseDetailsService caseDetailsService;
    private final NoticeService noticeService;

    @GetMapping("/api/cases/{caseId}/documents")
    public List<Document> listForCase(@PathVariable String caseId) {
        caseDetailsService.getById(caseId);
        return documentService.listForCase(caseId);
    }

    @PostMapping("/api/cases/{caseId}/documents")
    public Document uploadForCase(@PathVariable String caseId, @RequestParam("file") MultipartFile file) throws IOException {
        CaseDetails caseDetails = caseDetailsService.getById(caseId);
        return documentService.uploadForCase(caseDetails, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }

    @GetMapping("/api/cases/{caseId}/documents/{docId}/download")
    public ResponseEntity<byte[]> downloadForCase(@PathVariable String caseId, @PathVariable String docId) {
        caseDetailsService.getById(caseId);
        return toResponse(documentService.getById(docId));
    }

    @DeleteMapping("/api/cases/{caseId}/documents/{docId}")
    public void deleteForCase(@PathVariable String caseId, @PathVariable String docId) {
        caseDetailsService.getById(caseId);
        documentService.delete(docId);
    }

    @GetMapping("/api/notices/{noticeId}/documents")
    public List<Document> listForNotice(@PathVariable String noticeId) {
        noticeService.getById(noticeId);
        return documentService.listForNotice(noticeId);
    }

    @PostMapping("/api/notices/{noticeId}/documents")
    public Document uploadForNotice(@PathVariable String noticeId, @RequestParam("file") MultipartFile file) throws IOException {
        Notice notice = noticeService.getById(noticeId);
        return documentService.uploadForNotice(notice, "attachment", file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }

    @GetMapping("/api/notices/{noticeId}/documents/{docId}/download")
    public ResponseEntity<byte[]> downloadForNotice(@PathVariable String noticeId, @PathVariable String docId) {
        noticeService.getById(noticeId);
        return toResponse(documentService.getById(docId));
    }

    @DeleteMapping("/api/notices/{noticeId}/documents/{docId}")
    public void deleteForNotice(@PathVariable String noticeId, @PathVariable String docId) {
        noticeService.getById(noticeId);
        documentService.delete(docId);
    }

    private ResponseEntity<byte[]> toResponse(Document document) {
        MediaType type = document.getContentType() != null
                ? MediaType.parseMediaType(document.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(document.getData());
    }
}
