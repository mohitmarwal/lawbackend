package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, String> {

    Optional<Bill> findByBillNo(String billNo);

    List<Bill> findByBillNoContainingIgnoreCase(String billNo);

    List<Bill> findByBillDate(LocalDate billDate);

    List<Bill> findByBillDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<Bill> findByCaseDetailsId(String caseId);

    List<Bill> findByTotal(Double total);

    List<Bill> findByReceived(Double received);

    List<Bill> findByBalance(Double balance);

    List<Bill> findByBalanceGreaterThan(Double balance);

    List<Bill> findByBalanceLessThan(Double balance);

    List<Bill> findByReceivedGreaterThan(Double amount);

    List<Bill> findByTotalGreaterThan(Double amount);

    long countByCaseDetailsId(String caseId);

    long countByBalanceGreaterThan(Double balance);

    boolean existsByBillNo(String billNo);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<Bill> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<Bill> findByCaseDetailsId(String caseId, Pageable pageable);

    Page<Bill> findByBillDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Bill> findByBalanceGreaterThan(Double balance, Pageable pageable);

    Page<Bill> findByBillNoContainingIgnoreCase(String billNo, Pageable pageable);
}