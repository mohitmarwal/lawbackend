package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Task;
import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.TaskRepository;
import com.abhipsa.digital.law.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    // Used by the secondary filter/search endpoints below (status, priority,
    // type, etc. - not the main task list, see getAll()/getAllPaged()):
    // non-admins only see tasks assigned to them personally.
    private String scopeUserId() {
        return currentUserService.isAdmin() ? null : currentUserService.getUserId();
    }

    private void assertOwnership(Task task) {
        if (currentUserService.isAdmin()) return;
        String myId = currentUserService.getUserId();
        String assignedId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
        if (myId != null && myId.equals(assignedId)) return;
        if (currentUserService.isSeniorAssociate()) {
            String caseOwnerId = task.getCaseDetails() != null && task.getCaseDetails().getAssignedUser() != null
                    ? task.getCaseDetails().getAssignedUser().getId() : null;
            if (myId != null && myId.equals(caseOwnerId)) return;
        }
        throw new AccessDeniedException("This task is not assigned to you");
    }

    public Task create(Task task) {
        if (currentUserService.isAdmin()) {
            // unrestricted
        } else if (currentUserService.isSeniorAssociate()) {
            User requested = task.getAssignedTo();
            String requestedId = requested != null ? requested.getId() : null;
            if (requestedId == null || requestedId.isBlank()) {
                task.setAssignedTo(currentUserService.getUser());
            } else {
                User target = userRepository.findById(requestedId)
                        .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                if (!"associate".equalsIgnoreCase(target.getRole())) {
                    throw new AccessDeniedException("Senior associates can only assign tasks to associates");
                }
                task.setAssignedTo(target);
            }
        } else {
            // Plain associates can only ever create a task assigned to
            // themselves, regardless of what the "Assigned User" field said.
            task.setAssignedTo(currentUserService.getUser());
        }
        return repository.save(task);
    }

    public List<Task> getAll() {
        if (currentUserService.isAdmin()) {
            return repository.findAll();
        }
        String myId = currentUserService.getUserId();
        if (currentUserService.isSeniorAssociate()) {
            // Assigned to them personally, or on a case they own (they may
            // have delegated the task itself to an associate).
            return repository.findByAssignedToIdOrCaseDetails_AssignedUserId(myId, myId);
        }
        return repository.findByAssignedToId(myId);
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
        existing.setComments(task.getComments());
        // Only admin or senior associate can move a task to a different
        // case; a plain associate's own edits can't change it.
        if (currentUserService.isAdmin() || currentUserService.isSeniorAssociate()) {
            existing.setCaseDetails(task.getCaseDetails());
        }
        // Admin unrestricted; senior associate may move it only to an
        // associate (same rule as create()/reassign()); a plain associate's
        // own edits can't move the task off themselves.
        if (currentUserService.isAdmin()) {
            existing.setAssignedTo(task.getAssignedTo());
        } else if (currentUserService.isSeniorAssociate()) {
            User requested = task.getAssignedTo();
            String requestedId = requested != null ? requested.getId() : null;
            if (requestedId != null && !requestedId.isBlank()) {
                User target = userRepository.findById(requestedId)
                        .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                if (!"associate".equalsIgnoreCase(target.getRole())) {
                    throw new AccessDeniedException("Senior associates can only assign tasks to associates");
                }
                existing.setAssignedTo(target);
            }
        }
        existing.setAssignedBy(task.getAssignedBy());

        return repository.save(existing);
    }

    // Admin, or senior associate reassigning only to an associate, per
    // FR-TM02 (extended).
    public Task reassign(String id, User newAssignee) {
        User target;
        if (currentUserService.isAdmin()) {
            target = newAssignee;
        } else if (currentUserService.isSeniorAssociate()) {
            String targetId = newAssignee != null ? newAssignee.getId() : null;
            User loaded = targetId != null ? userRepository.findById(targetId).orElse(null) : null;
            if (loaded == null || !"associate".equalsIgnoreCase(loaded.getRole())) {
                throw new AccessDeniedException("Senior associates can only reassign tasks to associates");
            }
            target = loaded;
        } else {
            throw new AccessDeniedException("Only an admin or senior associate can reassign tasks");
        }

        // Fetch directly rather than via getById(): getById() enforces
        // assertOwnership() (task must currently be assigned to the caller),
        // which would incorrectly block a senior associate from reassigning
        // a task that currently belongs to a different associate.
        Task existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        existing.setAssignedTo(target);
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
        if (currentUserService.isAdmin()) {
            return repository.findAll(pageable);
        }
        String myId = currentUserService.getUserId();
        if (currentUserService.isSeniorAssociate()) {
            return repository.findByAssignedToIdOrCaseDetails_AssignedUserId(myId, myId, pageable);
        }
        return repository.findByAssignedToId(myId, pageable);
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