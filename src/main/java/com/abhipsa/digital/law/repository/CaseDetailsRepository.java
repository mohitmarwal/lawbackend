package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.CaseDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CaseDetailsRepository extends JpaRepository<CaseDetails, String> {

    Optional<CaseDetails> findByCaseNumber(String caseNumber);

    Optional<CaseDetails> findByOfficeFileNumber(String officeFileNumber);

    List<CaseDetails> findByStatus(String status);

    List<CaseDetails> findByPlaintiffClientIdOrDefendantClientId(String plaintiffClientId, String defendantClientId);

    Page<CaseDetails> findByPlaintiffClientIdOrDefendantClientId(String plaintiffClientId, String defendantClientId, Pageable pageable);

    List<CaseDetails> findByPlaintiffClient_NameContainingIgnoreCase(String plaintiff);

    List<CaseDetails> findByDefendantClient_NameContainingIgnoreCase(String defendant);

    List<CaseDetails> findByDescriptionContainingIgnoreCase(String description);

    List<CaseDetails> findByCourtId(String courtId);

    List<CaseDetails> findByAssignedUserId(String userId);

    List<CaseDetails> findByFilingDate(LocalDate filingDate);

    List<CaseDetails> findByNextDate(LocalDate nextDate);

    List<CaseDetails> findByFilingDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<CaseDetails> findByNextDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<CaseDetails> findByCaseNumberContainingIgnoreCase(
            String caseNumber);

    List<CaseDetails> findByOfficeFileNumberContainingIgnoreCase(
            String officeFileNumber);

    List<CaseDetails> findByStatusAndCourtId(
            String status,
            String courtId);

    List<CaseDetails> findByStatusAndAssignedUserId(
            String status,
            String userId);

    List<CaseDetails> findByCourtIdAndAssignedUserId(String courtId, String userId);

    List<CaseDetails> findByPlaintiffClient_NameContainingIgnoreCaseAndAssignedUserId(String plaintiff, String userId);

    List<CaseDetails> findByDefendantClient_NameContainingIgnoreCaseAndAssignedUserId(String defendant, String userId);

    List<CaseDetails> findByNextDateBetweenAndAssignedUserId(LocalDate startDate, LocalDate endDate, String userId);

    List<CaseDetails> findByFilingDateBetweenAndAssignedUserId(LocalDate startDate, LocalDate endDate, String userId);


    long countByCaseTypeIgnoreCase(String caseType);

    long countByLimitationDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
    SELECT COUNT(c)
    FROM CaseDetails c
    WHERE UPPER(c.caseType) IN ('SUIT','COMPLAINT')
""")
    long countFiledSuitComplaint();


    long countByStatus(String status);

    long countByCourtId(String courtId);

    long countByAssignedUserId(String userId);

    boolean existsByCaseNumber(String caseNumber);

    boolean existsByOfficeFileNumber(String officeFileNumber);

    @Query(value = """
        SELECT
            cd.office_file_number AS fileNo,
            cd.case_number AS regNo,
            CONCAT(pc.name, ' vs ', dc.name) AS parties,
            c.name AS court,
            cd.filing_date AS prevDate,
            cd.next_date AS nextDate
        FROM case_details cd
        LEFT JOIN court c ON cd.court_id = c.id
        LEFT JOIN client pc ON cd.plaintiff_client_id = pc.id
        LEFT JOIN client dc ON cd.defendant_client_id = dc.id
        WHERE DATE(cd.next_date) = :targetDate
          AND (:userId IS NULL OR cd.assigned_user_id = :userId)
        """, nativeQuery = true)
    List<Map<String, Object>> findDailyBoardData(@Param("targetDate") LocalDate targetDate, @Param("userId") String userId);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<CaseDetails> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<CaseDetails> findByStatus(String status, Pageable pageable);

    Page<CaseDetails> findByCourtId(String courtId, Pageable pageable);

    Page<CaseDetails> findByAssignedUserId(String userId, Pageable pageable);

    Page<CaseDetails> findByPlaintiffClient_NameContainingIgnoreCase(String plaintiff, Pageable pageable);

    Page<CaseDetails> findByDefendantClient_NameContainingIgnoreCase(String defendant, Pageable pageable);

    Page<CaseDetails> findByNextDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<CaseDetails> findByFilingDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<CaseDetails> findByStatusAndAssignedUserId(String status, String userId, Pageable pageable);

    Page<CaseDetails> findByCourtIdAndAssignedUserId(String courtId, String userId, Pageable pageable);

    Page<CaseDetails> findByPlaintiffClient_NameContainingIgnoreCaseAndAssignedUserId(String plaintiff, String userId, Pageable pageable);

    Page<CaseDetails> findByDefendantClient_NameContainingIgnoreCaseAndAssignedUserId(String defendant, String userId, Pageable pageable);

    Page<CaseDetails> findByNextDateBetweenAndAssignedUserId(LocalDate startDate, LocalDate endDate, String userId, Pageable pageable);

    Page<CaseDetails> findByFilingDateBetweenAndAssignedUserId(LocalDate startDate, LocalDate endDate, String userId, Pageable pageable);
}