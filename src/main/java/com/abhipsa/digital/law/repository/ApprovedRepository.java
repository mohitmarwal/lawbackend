package com.abhipsa.digital.law.repository;


import com.abhipsa.digital.law.entity.Approved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApprovedRepository extends JpaRepository<Approved, String> {

    List<Approved> findByApproved(boolean approved);

    List<Approved> findByEnteredBy(String enteredBy);

    List<Approved> findByEnteredByContainingIgnoreCase(String enteredBy);

    List<Approved> findBySummaryContainingIgnoreCase(String summary);

    List<Approved> findByCaseDetailsId(String caseId);

    List<Approved> findByCourtId(String courtId);

    List<Approved> findByHearingDate(LocalDate hearingDate);

    List<Approved> findByNextDate(LocalDate nextDate);

    List<Approved> findByHearingDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<Approved> findByNextDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<Approved> findByHearingDateGreaterThanEqual(
            LocalDate date);

    List<Approved> findByNextDateGreaterThanEqual(
            LocalDate date);

    List<Approved> findByApprovedAndCourtId(
            boolean approved,
            String courtId);

    List<Approved> findByApprovedAndCaseDetailsId(
            boolean approved,
            String caseId);

    long countByApproved(boolean approved);

    long countByCourtId(String courtId);

    long countByCaseDetailsId(String caseId);

    boolean existsByCaseDetailsId(String caseId);
}
