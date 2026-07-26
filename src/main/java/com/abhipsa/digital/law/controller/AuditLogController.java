package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.AuditLog;
import com.abhipsa.digital.law.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    @GetMapping("/paged")
    public Page<AuditLog> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public List<AuditLog> findByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {

        return service.findByEntity(entityType, entityId);
    }

    @GetMapping("/entity/{entityType}/paged")
    public Page<AuditLog> findByEntityTypePaged(
            @PathVariable String entityType,
            @PageableDefault(size = 20) Pageable pageable) {

        return service.findByEntityTypePaged(entityType, pageable);
    }

    @GetMapping("/actor/{actor}/paged")
    public Page<AuditLog> findByActorPaged(
            @PathVariable String actor,
            @PageableDefault(size = 20) Pageable pageable) {

        return service.findByActorPaged(actor, pageable);
    }
}
