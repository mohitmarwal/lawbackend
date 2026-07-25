package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Task;
import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final CurrentUserService currentUserService;

    // Non-admins only see tasks assigned to them.
    private String scopeUserId() {
        return currentUserService.isAdmin() ? null : currentUserService.getUserId();
    }

    private void assertOwnership(Task task) {
        if (currentUserService.isAdmin()) return;
        String myId = currentUserService.getUserId();
        String assignedId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
        if (myId == null || !myId.equals(assignedId)) {
            throw new AccessDeniedException("This task is not assigned to you");
        }
    }

    public Task create(Task task) {
        if (!currentUserService.isAdmin()) {
            // Non-admins can only ever create a task assigned to themselves,
            // regardless of what the "Assigned User" field said.
            task.setAssignedTo(currentUserService.getUser());
        }
        return repository.save(task);
    }

    public List<Task> getAll() {
        String myId = scopeUserId();
        return myId == null ? repository.findAll() : repository.findByAssignedToId(myId);
    }

    public Task getById(String id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        assertOwnership(task);
        return task;
    }

    public Task update(String id, Task task) {

        Task existing = getById(id);

        existing.setTask(task.getTask());
        existing.setType(task.getType());
        existing.setPriority(task.getPriority());
        existing.setStatus(task.getStatus());
        existing.setDueDate(task.getDueDate());
        existing.setEndTime(task.getEndTime());
        existing.setCaseDetails(task.getCaseDetails());
        // Reassigning to someone else goes through the admin-only
        // reassign() endpoint; a non-admin's own edits can't move the task
        // off themselves.
        if (currentUserService.isAdmin()) {
            existing.setAssignedTo(task.getAssignedTo());
        }
        existing.setAssignedBy(task.getAssignedBy());

        return repository.save(existing);
    }

    // Admin-only re-assignment, per FR-TM02.
    public Task reassign(String id, User newAssignee) {
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Only an admin can reassign tasks");
        }
        Task existing = getById(id);
        existing.setAssignedTo(newAssignee);
        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<Task> findByStatus(String status) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByStatusContainingIgnoreCase(status)
                : repository.findByStatusAndAssignedToId(status, myId);
    }

    public List<Task> findByPriority(String priority) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByPriorityContainingIgnoreCase(priority)
                : repository.findByPriorityContainingIgnoreCaseAndAssignedToId(priority, myId);
    }

    public List<Task> findByType(String type) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByTypeContainingIgnoreCase(type)
                : repository.findByTypeContainingIgnoreCaseAndAssignedToId(type, myId);
    }

    // Non-admins can only ever query their own assignment, regardless of
    // which userId they pass in.
    public List<Task> findByAssignedUser(String userId) {
        String myId = scopeUserId();
        return repository.findByAssignedToId(myId != null ? myId : userId);
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
        String myId = scopeUserId();
        return myId == null ? repository.findAll(pageable) : repository.findByAssignedToId(myId, pageable);
    }

    public Page<Task> findByStatusPaged(String status, Pageable pageable) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByStatusContainingIgnoreCase(status, pageable)
                : repository.findByStatusAndAssignedToId(status, myId, pageable);
    }

    public Page<Task> findByPriorityPaged(String priority, Pageable pageable) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByPriorityContainingIgnoreCase(priority, pageable)
                : repository.findByPriorityContainingIgnoreCaseAndAssignedToId(priority, myId, pageable);
    }

    public Page<Task> findByTypePaged(String type, Pageable pageable) {
        String myId = scopeUserId();
        return myId == null
                ? repository.findByTypeContainingIgnoreCase(type, pageable)
                : repository.findByTypeContainingIgnoreCaseAndAssignedToId(type, myId, pageable);
    }

    // Non-admins can only ever query their own assignment, regardless of
    // which userId they pass in.
    public Page<Task> findByAssignedUserPaged(String userId, Pageable pageable) {
        String myId = scopeUserId();
        return repository.findByAssignedToId(myId != null ? myId : userId, pageable);
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