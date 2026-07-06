package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Bill;
import com.abhipsa.digital.law.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository repository;

    public Bill create(Bill bill) {
        return repository.save(bill);
    }

    public List<Bill> getAll() {
        return repository.findAll();
    }

    public Bill getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
    }

    public Bill update(String id, Bill bill) {

        Bill existing = getById(id);

        existing.setBillNo(bill.getBillNo());
        existing.setBillDate(bill.getBillDate());
        existing.setTotal(bill.getTotal());
        existing.setReceived(bill.getReceived());
        existing.setBalance(bill.getBalance());
        existing.setCaseDetails(bill.getCaseDetails());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public Bill findByBillNo(String billNo) {
        return repository.findByBillNo(billNo)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
    }

    public List<Bill> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<Bill> findByBillDate(LocalDate billDate) {
        return repository.findByBillDate(billDate);
    }

    public List<Bill> findByBillDateBetween(
            LocalDate start,
            LocalDate end) {
        return repository.findByBillDateBetween(start, end);
    }

    public List<Bill> findPendingBills() {
        return repository.findByBalanceGreaterThan(0.0);
    }

    public long countBillsForCase(String caseId) {
        return repository.countByCaseDetailsId(caseId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<Bill> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Bill> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }

    public Page<Bill> findByBillDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {
        return repository.findByBillDateBetween(start, end, pageable);
    }

    public Page<Bill> findPendingBillsPaged(Pageable pageable) {
        return repository.findByBalanceGreaterThan(0.0, pageable);
    }

    public Page<Bill> findByBillNoPaged(String billNo, Pageable pageable) {
        return repository.findByBillNoContainingIgnoreCase(billNo, pageable);
    }
}