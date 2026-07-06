package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Associate;
import com.abhipsa.digital.law.service.AssociateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/associates")
@RequiredArgsConstructor
public class AssociateController {

    private final AssociateService service;

    @PostMapping
    public Associate create(@RequestBody Associate associate) {
        return service.create(associate);
    }

    @GetMapping
    public List<Associate> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Associate getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Associate update(
            @PathVariable String id,
            @RequestBody Associate associate) {

        return service.update(id, associate);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/email/{email}")
    public Optional<Associate> findByEmail(
            @PathVariable String email) {

        return service.findByEmail(email);
    }

    @GetMapping("/mobile/{mobile}")
    public Optional<Associate> findByMobile(
            @PathVariable String mobile) {

        return service.findByMobile(mobile);
    }

    @GetMapping("/name/{name}")
    public List<Associate> findByName(
            @PathVariable String name) {

        return service.findByName(name);
    }

    @GetMapping("/designation/{designation}")
    public List<Associate> findByDesignation(
            @PathVariable String designation) {

        return service.findByDesignation(designation);
    }

    @GetMapping("/case/{caseId}")
    public List<Associate> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/pending-cases/{count}")
    public List<Associate> findByPendingCases(
            @PathVariable int count) {

        return service.findByPendingCasesGreaterThan(count);
    }

    @GetMapping("/open-tasks/{count}")
    public List<Associate> findByOpenTasks(
            @PathVariable int count) {

        return service.findByOpenTasksGreaterThan(count);
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/associates/paged?page=0&size=20&sort=name,asc
    // ==================================================================

    @GetMapping("/paged")
    public Page<Associate> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/name/{name}/paged")
    public Page<Associate> findByNamePaged(
            @PathVariable String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByNamePaged(name, pageable);
    }

    @GetMapping("/designation/{designation}/paged")
    public Page<Associate> findByDesignationPaged(
            @PathVariable String designation,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByDesignationPaged(designation, pageable);
    }

    @GetMapping("/case/{caseId}/paged")
    public Page<Associate> findByCaseIdPaged(
            @PathVariable String caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCaseIdPaged(caseId, pageable);
    }

    @GetMapping("/pending-cases/{count}/paged")
    public Page<Associate> findByPendingCasesPaged(
            @PathVariable int count,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByPendingCasesGreaterThanPaged(count, pageable);
    }

    @GetMapping("/open-tasks/{count}/paged")
    public Page<Associate> findByOpenTasksPaged(
            @PathVariable int count,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByOpenTasksGreaterThanPaged(count, pageable);
    }
}