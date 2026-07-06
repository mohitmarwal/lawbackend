package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByTaskContainingIgnoreCase(String task);

    List<Task> findByType(String type);

    List<Task> findByTypeContainingIgnoreCase(String type);

    List<Task> findByPriority(String priority);

    List<Task> findByPriorityContainingIgnoreCase(String priority);

    List<Task> findByStatus(String status);

    List<Task> findByStatusContainingIgnoreCase(String status);

    List<Task> findByAssignedToId(String userId);

    List<Task> findByCaseDetailsId(String caseId);

    List<Task> findByDueDate(LocalDate dueDate);

    List<Task> findByDueDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    List<Task> findByDueDateLessThanEqual(LocalDate dueDate);

    List<Task> findByDueDateGreaterThanEqual(LocalDate dueDate);

    List<Task> findByStatusAndAssignedToId(
            String status,
            String userId);

    List<Task> findByPriorityAndStatus(
            String priority,
            String status);

    List<Task> findByCaseDetailsIdAndStatus(
            String caseId,
            String status);

    long countByStatus(String status);

    long countByAssignedToId(String userId);

    long countByCaseDetailsId(String caseId);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<Task> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<Task> findByStatusContainingIgnoreCase(String status, Pageable pageable);

    Page<Task> findByPriorityContainingIgnoreCase(String priority, Pageable pageable);

    Page<Task> findByTypeContainingIgnoreCase(String type, Pageable pageable);

    Page<Task> findByAssignedToId(String userId, Pageable pageable);

    Page<Task> findByCaseDetailsId(String caseId, Pageable pageable);

    Page<Task> findByDueDate(LocalDate dueDate, Pageable pageable);

    Page<Task> findByDueDateLessThanEqual(LocalDate dueDate, Pageable pageable);
}