package com.abhipsa.digital.law.repository;


import com.abhipsa.digital.law.entity.HearingDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HearingDateRepository extends JpaRepository<HearingDate, String> {

    List<HearingDate> findByApproved(boolean approved);

    Page<HearingDate> findByApproved(boolean approved, Pageable pageable);

    Page<HearingDate> findByCaseDetailsId(String caseId, Pageable pageable);

    List<HearingDate> findBySummaryContainingIgnoreCase(String summary);

    List<HearingDate> findByCaseDetailsId(String caseId);

    // Used to batch-enrich a page of cases with their most recent prior
    // hearing date (ordered so the first entry per case id is the latest).
    List<HearingDate> findByCaseDetailsIdInOrderByHearingDateDesc(List<String> caseIds);

    List<HearingDate> findByCourtId(String courtId);

    List<HearingDate> findBySubmittedById(String userId);

    List<HearingDate> findByHearingDate(LocalDate hearingDate);

    List<HearingDate> findByNextDate(LocalDate nextDate);

    List<HearingDate> findByHearingDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<HearingDate> findByNextDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<HearingDate> findByHearingDateGreaterThanEqual(
            LocalDate date);

    List<HearingDate> findByNextDateGreaterThanEqual(
            LocalDate date);

    List<HearingDate> findByApprovedAndCourtId(
            boolean approved,
            String courtId);

    List<HearingDate> findByApprovedAndCaseDetailsId(
            boolean approved,
            String caseId);

    long countByApproved(boolean approved);

    long countByCourtId(String courtId);

    long countByCaseDetailsId(String caseId);

    boolean existsByCaseDetailsId(String caseId);
}
