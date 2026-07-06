package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, String> {

    Optional<Notice> findByReferenceNo(String referenceNo);

    List<Notice> findByOfficeFileNo(String officeFileNo);

    List<Notice> findByOfficeFileNoContainingIgnoreCase(String officeFileNo);

    List<Notice> findByReferenceNoContainingIgnoreCase(String referenceNo);

    List<Notice> findByCaseDetailsId(String caseId);

    List<Notice> findByNoticeDate(LocalDate noticeDate);

    List<Notice> findByDispatchDate(LocalDate dispatchDate);

    List<Notice> findByNoticeDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<Notice> findByDispatchDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<Notice> findByNoticeDateGreaterThanEqual(LocalDate date);

    List<Notice> findByDispatchDateGreaterThanEqual(LocalDate date);

    long countByCaseDetailsId(String caseId);

    boolean existsByReferenceNo(String referenceNo);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<Notice> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<Notice> findByOfficeFileNoContainingIgnoreCase(String officeFileNo, Pageable pageable);

    Page<Notice> findByCaseDetailsId(String caseId, Pageable pageable);

    Page<Notice> findByNoticeDate(LocalDate noticeDate, Pageable pageable);

    Page<Notice> findByDispatchDate(LocalDate dispatchDate, Pageable pageable);

    Page<Notice> findByNoticeDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Notice> findByDispatchDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}