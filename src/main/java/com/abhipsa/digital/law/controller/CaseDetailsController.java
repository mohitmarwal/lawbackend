package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.service.CaseDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseDetailsController {

    private final CaseDetailsService service;

    @PostMapping
    public CaseDetails create(@RequestBody CaseDetails caseDetails) {
        return service.create(caseDetails);
    }

    @GetMapping
    public List<CaseDetails> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public CaseDetails getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public CaseDetails update(
            @PathVariable String id,
            @RequestBody CaseDetails caseDetails) {

        return service.update(id, caseDetails);
    }

    @GetMapping("/daily-board")
    public List<Map<String, Object>> getDailyBoard(@RequestParam(value = "date", required = false) String date) {
        if (date == null) return Collections.emptyList();
        try {
            LocalDate localDate = LocalDate.parse(date);
            return service.getDailyBoardData(localDate);
        } catch (Exception e) {
            e.printStackTrace(); // Check your Spring Console for this error!
            return Collections.emptyList();
        }
    }

    @PostMapping("/checker/approve/{id}")
    public ResponseEntity<?> approveEntry(@PathVariable String id) {
        try {
            service.approveCase(id);   // Implement in service
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Approval failed");
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/case-number/{caseNumber}")
    public CaseDetails findByCaseNumber(
            @PathVariable String caseNumber) {

        return service.findByCaseNumber(caseNumber);
    }

    @GetMapping("/file-number/{officeFileNumber}")
    public CaseDetails findByOfficeFileNumber(
            @PathVariable String officeFileNumber) {

        return service.findByOfficeFileNumber(officeFileNumber);
    }

    @GetMapping("/status/{status}")
    public List<CaseDetails> findByStatus(
            @PathVariable String status) {

        return service.findByStatus(status);
    }

    @GetMapping("/court/{courtId}")
    public List<CaseDetails> findByCourtId(
            @PathVariable String courtId) {

        return service.findByCourtId(courtId);
    }

    @GetMapping("/lawyer/{userId}")
    public List<CaseDetails> findByAssignedUser(
            @PathVariable String userId) {

        return service.findByAssignedUserId(userId);
    }

    @GetMapping("/plaintiff/{plaintiff}")
    public List<CaseDetails> findByPlaintiff(
            @PathVariable String plaintiff) {

        return service.findByPlaintiff(plaintiff);
    }

    @GetMapping("/defendant/{defendant}")
    public List<CaseDetails> findByDefendant(
            @PathVariable String defendant) {

        return service.findByDefendant(defendant);
    }

    @GetMapping("/filing-date-range")
    public List<CaseDetails> findByFilingDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end) {

        return service.findByFilingDateBetween(start, end);
    }

    @GetMapping("/next-date-range")
    public List<CaseDetails> findByNextDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end) {

        return service.findByNextDateBetween(start, end);
    }

    @GetMapping("/filed-suit-complaint")
    public long getFiledSuitComplaint() {
        return service.countFiledSuitComplaint();
    }

    @GetMapping("/limitation-begins")
    public long getLimitationBegins() {
        return service.countLimitationBegins();
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/cases/paged?page=0&size=20&sort=nextDate,desc
    // ==================================================================

    @GetMapping("/paged")
    public Page<CaseDetails> getAllPaged(
            @PageableDefault(size = 20, sort = "filingDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/status/{status}/paged")
    public Page<CaseDetails> findByStatusPaged(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByStatusPaged(status, pageable);
    }

    @GetMapping("/court/{courtId}/paged")
    public Page<CaseDetails> findByCourtIdPaged(
            @PathVariable String courtId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCourtIdPaged(courtId, pageable);
    }

    @GetMapping("/lawyer/{userId}/paged")
    public Page<CaseDetails> findByAssignedUserPaged(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByAssignedUserIdPaged(userId, pageable);
    }

    @GetMapping("/plaintiff/{plaintiff}/paged")
    public Page<CaseDetails> findByPlaintiffPaged(
            @PathVariable String plaintiff,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByPlaintiffPaged(plaintiff, pageable);
    }

    @GetMapping("/defendant/{defendant}/paged")
    public Page<CaseDetails> findByDefendantPaged(
            @PathVariable String defendant,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByDefendantPaged(defendant, pageable);
    }

    @GetMapping("/next-date-range/paged")
    public Page<CaseDetails> findByNextDateRangePaged(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            @PageableDefault(size = 20) Pageable pageable) {

        return service.findByNextDateBetweenPaged(start, end, pageable);
    }

    @GetMapping("/filing-date-range/paged")
    public Page<CaseDetails> findByFilingDateRangePaged(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            @PageableDefault(size = 20) Pageable pageable) {

        return service.findByFilingDateBetweenPaged(start, end, pageable);
    }
}