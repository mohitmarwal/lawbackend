package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.HearingDate;
import com.abhipsa.digital.law.service.HearingDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hearing-dates")
@RequiredArgsConstructor
public class HearingDateController {

    private final HearingDateService service;

    @PostMapping
    public HearingDate create(@RequestBody HearingDate hearingDate) {
        return service.create(hearingDate);
    }

    @GetMapping
    public List<HearingDate> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public HearingDate getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public HearingDate update(
            @PathVariable String id,
            @RequestBody HearingDate hearingDate) {

        return service.update(id, hearingDate);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PostMapping("/{id}/approve")
    public HearingDate approve(@PathVariable String id) {
        return service.approve(id);
    }

    @GetMapping("/status/{approved}")
    public List<HearingDate> findByApproved(
            @PathVariable boolean approved) {

        return service.findByApproved(approved);
    }

    @GetMapping("/case/{caseId}")
    public List<HearingDate> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/court/{courtId}")
    public List<HearingDate> findByCourtId(
            @PathVariable String courtId) {

        return service.findByCourtId(courtId);
    }

    @GetMapping("/submitted-by/{userId}")
    public List<HearingDate> findBySubmittedBy(
            @PathVariable String userId) {

        return service.findBySubmittedBy(userId);
    }

    // ==================================================================
    // ---- Pagination support ----
    // Example usage: GET /api/hearing-dates/paged?page=0&size=20&sort=hearingDate,desc
    // ==================================================================

    @GetMapping("/paged")
    public Page<HearingDate> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/case/{caseId}/paged")
    public Page<HearingDate> findByCaseIdPaged(
            @PathVariable String caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCaseIdPaged(caseId, pageable);
    }

    @GetMapping("/status/{approved}/paged")
    public Page<HearingDate> findByApprovedPaged(
            @PathVariable boolean approved,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByApprovedPaged(approved, pageable);
    }
}
