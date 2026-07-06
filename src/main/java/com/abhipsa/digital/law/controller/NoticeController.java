package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService service;

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