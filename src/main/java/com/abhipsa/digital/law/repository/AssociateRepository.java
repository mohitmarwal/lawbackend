package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Associate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssociateRepository extends JpaRepository<Associate, String> {

    Optional<Associate> findByEmail(String email);

    Optional<Associate> findByMobile(String mobile);

    List<Associate> findByName(String name);

    List<Associate> findByNameContainingIgnoreCase(String name);

    List<Associate> findByDesignation(String designation);

    List<Associate> findByDesignationContainingIgnoreCase(String designation);

    List<Associate> findByCaseDetailsId(String caseId);

    List<Associate> findByPendingCases(int pendingCases);

    List<Associate> findByOpenTasks(int openTasks);

    List<Associate> findByPendingCasesGreaterThan(int pendingCases);

    List<Associate> findByOpenTasksGreaterThan(int openTasks);

    List<Associate> findByPendingCasesLessThan(int pendingCases);

    List<Associate> findByOpenTasksLessThan(int openTasks);

    List<Associate> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    long countByCaseDetailsId(String caseId);

    long countByDesignation(String designation);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<Associate> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<Associate> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Associate> findByDesignationContainingIgnoreCase(String designation, Pageable pageable);

    Page<Associate> findByCaseDetailsId(String caseId, Pageable pageable);

    Page<Associate> findByPendingCasesGreaterThan(int pendingCases, Pageable pageable);

    Page<Associate> findByOpenTasksGreaterThan(int openTasks, Pageable pageable);
}