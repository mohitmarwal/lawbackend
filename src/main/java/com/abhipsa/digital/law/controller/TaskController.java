package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Task;
import com.abhipsa.digital.law.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    @PostMapping
    public Task create(@RequestBody Task task) {
        return service.create(task);
    }

    @GetMapping
    public List<Task> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Task update(
            @PathVariable String id,
            @RequestBody Task task) {

        return service.update(id, task);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/status/{status}")
    public List<Task> findByStatus(
            @PathVariable String status) {

        return service.findByStatus(status);
    }

    @GetMapping("/priority/{priority}")
    public List<Task> findByPriority(
            @PathVariable String priority) {

        return service.findByPriority(priority);
    }

    @GetMapping("/type/{type}")
    public List<Task> findByType(
            @PathVariable String type) {

        return service.findByType(type);
    }

    @GetMapping("/user/{userId}")
    public List<Task> findByAssignedUser(
            @PathVariable String userId) {

        return service.findByAssignedUser(userId);
    }

    @GetMapping("/case/{caseId}")
    public List<Task> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/due-date/{dueDate}")
    public List<Task> findByDueDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueDate) {

        return service.findByDueDate(dueDate);
    }

    @GetMapping("/overdue")
    public List<Task> findOverdueTasks() {
        return service.findOverdueTasks();
    }

    @GetMapping("/count/status/{status}")
    public long countByStatus(
            @PathVariable String status) {

        return service.countByStatus(status);
    }

    @GetMapping("/count/user/{userId}")
    public long countByAssignedUser(
            @PathVariable String userId) {

        return service.countByAssignedUser(userId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/tasks/paged?page=0&size=20&sort=dueDate,asc
    // ==================================================================

    @GetMapping("/paged")
    public Page<Task> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/status/{status}/paged")
    public Page<Task> findByStatusPaged(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByStatusPaged(status, pageable);
    }

    @GetMapping("/priority/{priority}/paged")
    public Page<Task> findByPriorityPaged(
            @PathVariable String priority,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByPriorityPaged(priority, pageable);
    }

    @GetMapping("/type/{type}/paged")
    public Page<Task> findByTypePaged(
            @PathVariable String type,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByTypePaged(type, pageable);
    }

    @GetMapping("/user/{userId}/paged")
    public Page<Task> findByAssignedUserPaged(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByAssignedUserPaged(userId, pageable);
    }

    @GetMapping("/case/{caseId}/paged")
    public Page<Task> findByCaseIdPaged(
            @PathVariable String caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCaseIdPaged(caseId, pageable);
    }

    @GetMapping("/due-date/{dueDate}/paged")
    public Page<Task> findByDueDatePaged(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByDueDatePaged(dueDate, pageable);
    }

    @GetMapping("/overdue/paged")
    public Page<Task> findOverdueTasksPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.findOverdueTasksPaged(pageable);
    }
}