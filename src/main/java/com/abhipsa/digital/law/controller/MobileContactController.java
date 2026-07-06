package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.MobileContact;
import com.abhipsa.digital.law.service.MobileContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile-contacts")
@RequiredArgsConstructor
public class MobileContactController {

    private final MobileContactService service;

    @PostMapping
    public MobileContact create(@RequestBody MobileContact mobileContact) {
        return service.create(mobileContact);
    }

    @GetMapping
    public List<MobileContact> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public MobileContact getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public MobileContact update(
            @PathVariable String id,
            @RequestBody MobileContact mobileContact) {

        return service.update(id, mobileContact);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/mobile/{mobile}")
    public MobileContact findByMobile(
            @PathVariable String mobile) {

        return service.findByMobile(mobile);
    }

    @GetMapping("/name/{name}")
    public List<MobileContact> findByName(
            @PathVariable String name) {

        return service.findByName(name);
    }

    @GetMapping("/role/{role}")
    public List<MobileContact> findByRole(
            @PathVariable String role) {

        return service.findByRole(role);
    }

    @GetMapping("/case/{caseId}")
    public List<MobileContact> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/count/role/{role}")
    public long countByRole(
            @PathVariable String role) {

        return service.countByRole(role);
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/mobile-contacts/paged?page=0&size=20&sort=name,asc
    // ==================================================================

    @GetMapping("/paged")
    public Page<MobileContact> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/name/{name}/paged")
    public Page<MobileContact> findByNamePaged(
            @PathVariable String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByNamePaged(name, pageable);
    }

    @GetMapping("/role/{role}/paged")
    public Page<MobileContact> findByRolePaged(
            @PathVariable String role,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByRolePaged(role, pageable);
    }

    @GetMapping("/case/{caseId}/paged")
    public Page<MobileContact> findByCaseIdPaged(
            @PathVariable String caseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByCaseIdPaged(caseId, pageable);
    }
}