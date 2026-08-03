package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Document;
import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.service.DocumentService;
import com.abhipsa.digital.law.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService service;
    private final DocumentService documentService;

    @PostMapping
    public Notice create(@RequestBody Notice notice) {
        return service.create(notice);
    }

    @GetMapping
    public List<Notice> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Notice getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Notice update(
            @PathVariable String id,
            @RequestBody Notice notice) {

        return service.update(id, notice);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PostMapping("/{id}/approve")
    public Notice approve(
            @PathVariable String id,
            @RequestParam(required = false) String approverId) {

        User approvedBy = null;
        if (approverId != null) {
            approvedBy = new User();
            approvedBy.setId(approverId);
        }
        return service.approve(id, approvedBy);
    }

    @PostMapping("/{id}/deliver")
    public Notice markDelivered(@PathVariable String id) {
        return service.markDelivered(id);
    }

    @PostMapping("/{id}/document")
    public Notice uploadDocument(@PathVariable String id, @RequestParam("file") MultipartFile file) throws IOException {
        return service.saveDocument(id, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id) {
        service.getById(id); // ownership check
        Document document = documentService.findForNoticeSlot(id, "document")
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return toResponse(document);
    }

    @PostMapping("/{id}/receipt")
    public Notice uploadReceipt(@PathVariable String id, @RequestParam("file") MultipartFile file) throws IOException {
        return service.saveReceipt(id, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String id) {
        service.getById(id); // ownership check
        Document document = documentService.findForNoticeSlot(id, "receipt")
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return toResponse(document);
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

    @GetMapping("/reference/{referenceNo}")
    public Notice findByReferenceNo(
            @PathVariable String referenceNo) {

        return service.findByReferenceNo(referenceNo);
    }

    @GetMapping("/office-file/{officeFileNo}")
    public List<Notice> findByOfficeFileNo(
            @PathVariable String officeFileNo) {

        return service.findByOfficeFileNo(officeFileNo);
    }

    @GetMapping("/case/{caseId}")
    public List<Notice> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/notice-date/{noticeDate}")
    public List<Notice> findByNoticeDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate noticeDate) {

        return service.findByNoticeDate(noticeDate);
    }

    @GetMapping("/dispatch-date/{dispatchDate}")
    public List<Notice> findByDispatchDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dispatchDate) {

        return service.findByDispatchDate(dispatchDate);
    }

    @GetMapping("/count/case/{caseId}")
    public long countByCaseId(
            @PathVariable String caseId) {

        return service.countByCaseId(caseId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/notices/paged?page=0&size=20&sort=noticeDate,desc
    // ==================================================================

    @GetMapping("/paged")
    public Page<Notice> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/office-file/{officeFileNo}/paged")
    public Page<Notice> findByOfficeFileNoPaged(
            @PathVariable String officeFileNo,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByOfficeFileNoPaged(officeFileNo, pageable);
    }

    @GetMapping("/case/{caseId}/paged")
    public Page<Notice> findByCaseIdPaged(
            @PathVariable String caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCaseIdPaged(caseId, pageable);
    }

    @GetMapping("/notice-date/{noticeDate}/paged")
    public Page<Notice> findByNoticeDatePaged(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate noticeDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByNoticeDatePaged(noticeDate, pageable);
    }

    @GetMapping("/dispatch-date/{dispatchDate}/paged")
    public Page<Notice> findByDispatchDatePaged(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dispatchDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByDispatchDatePaged(dispatchDate, pageable);
    }

    @GetMapping("/notice-date-range/paged")
    public Page<Notice> findByNoticeDateRangePaged(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            @PageableDefault(size = 20) Pageable pageable) {

        return service.findByNoticeDateBetweenPaged(start, end, pageable);
    }

    @GetMapping("/dispatch-date-range/paged")
    public Page<Notice> findByDispatchDateRangePaged(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            @PageableDefault(size = 20) Pageable pageable) {

        return service.findByDispatchDateBetweenPaged(start, end, pageable);
    }
}