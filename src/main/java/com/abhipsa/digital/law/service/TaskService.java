package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Task;
import com.abhipsa.digital.law.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;

    public Task create(Task task) {
        return repository.save(task);
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public Task getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task update(String id, Task task) {

        Task existing = getById(id);

        existing.setTask(task.getTask());
        existing.setType(task.getType());
        existing.setPriority(task.getPriority());
        existing.setStatus(task.getStatus());
        existing.setDueDate(task.getDueDate());
        existing.setCaseDetails(task.getCaseDetails());
        existing.setAssignedTo(task.getAssignedTo());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<Task> findByStatus(String status) {
        return repository.findByStatusContainingIgnoreCase(status);
    }

    public List<Task> findByPriority(String priority) {
        return repository.findByPriorityContainingIgnoreCase(priority);
    }

    public List<Task> findByType(String type) {
        return repository.findByTypeContainingIgnoreCase(type);
    }

    public List<Task> findByAssignedUser(String userId) {
        return repository.findByAssignedToId(userId);
    }

    public List<Task> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<Task> findByDueDate(LocalDate dueDate) {
        return repository.findByDueDate(dueDate);
    }

    public List<Task> findByDueDateBetween(
            LocalDate start,
            LocalDate end) {
        return repository.findByDueDateBetween(start, end);
    }

    public List<Task> findOverdueTasks() {
        return repository.findByDueDateLessThanEqual(LocalDate.now());
    }

    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    public long countByAssignedUser(String userId) {
        return repository.countByAssignedToId(userId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<Task> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Task> findByStatusPaged(String status, Pageable pageable) {
        return repository.findByStatusContainingIgnoreCase(status, pageable);
    }

    public Page<Task> findByPriorityPaged(String priority, Pageable pageable) {
        return repository.findByPriorityContainingIgnoreCase(priority, pageable);
    }

    public Page<Task> findByTypePaged(String type, Pageable pageable) {
        return repository.findByTypeContainingIgnoreCase(type, pageable);
    }

    public Page<Task> findByAssignedUserPaged(String userId, Pageable pageable) {
        return repository.findByAssignedToId(userId, pageable);
    }

    public Page<Task> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }

    public Page<Task> findByDueDatePaged(LocalDate dueDate, Pageable pageable) {
        return repository.findByDueDate(dueDate, pageable);
    }

    public Page<Task> findOverdueTasksPaged(Pageable pageable) {
        return repository.findByDueDateLessThanEqual(LocalDate.now(), pageable);
    }
}