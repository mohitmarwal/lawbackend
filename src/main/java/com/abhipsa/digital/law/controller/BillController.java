package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Bill;
import com.abhipsa.digital.law.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService service;

    @PostMapping
    public Bill create(@RequestBody Bill bill) {
        return service.create(bill);
    }

    @GetMapping
    public List<Bill> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Bill getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Bill update(
            @PathVariable String id,
            @RequestBody Bill bill) {

        return service.update(id, bill);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/bill-no/{billNo}")
    public Bill findByBillNo(
            @PathVariable String billNo) {

        return service.findByBillNo(billNo);
    }

    @GetMapping("/case/{caseId}")
    public List<Bill> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/date/{billDate}")
    public List<Bill> findByBillDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate billDate) {

        return service.findByBillDate(billDate);
    }

    @GetMapping("/pending")
    public List<Bill> pendingBills() {
        return service.findPendingBills();
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/bills/paged?page=0&size=20&sort=billDate,desc
    // ==================================================================

    @GetMapping("/paged")
    public Page<Bill> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/case/{caseId}/paged")
    public Page<Bill> findByCaseIdPaged(
            @PathVariable String caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCaseIdPaged(caseId, pageable);
    }

    @GetMapping("/pending/paged")
    public Page<Bill> pendingBillsPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.findPendingBillsPaged(pageable);
    }

    @GetMapping("/bill-no/{billNo}/paged")
    public Page<Bill> findByBillNoPaged(
            @PathVariable String billNo,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByBillNoPaged(billNo, pageable);
    }
}