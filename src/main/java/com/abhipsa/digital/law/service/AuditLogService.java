package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.AuditLog;
import com.abhipsa.digital.law.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    public Page<AuditLog> getAllPaged(Pageable pageable) {
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<AuditLog> findByEntity(String entityType, String entityId) {
        return repository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    public Page<AuditLog> findByEntityTypePaged(String entityType, Pageable pageable) {
        return repository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable);
    }

    public Page<AuditLog> findByActorPaged(String actor, Pageable pageable) {
        return repository.findByActorOrderByCreatedAtDesc(actor, pageable);
    }
}
