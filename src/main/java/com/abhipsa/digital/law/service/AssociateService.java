package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Associate;
import com.abhipsa.digital.law.repository.AssociateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssociateService {

    private final AssociateRepository repository;

    public Associate create(Associate associate) {
        return repository.save(associate);
    }

    public List<Associate> getAll() {
        return repository.findAll();
    }

    public Associate getById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Associate not found"));
    }

    public Associate update(String id, Associate associate) {

        Associate existing = getById(id);

        associate.setId(existing.getId());

        return repository.save(associate);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public Optional<Associate> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Associate> findByMobile(String mobile) {
        return repository.findByMobile(mobile);
    }

    public List<Associate> findByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public List<Associate> findByDesignation(String designation) {
        return repository.findByDesignationContainingIgnoreCase(designation);
    }

    public List<Associate> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<Associate> findByPendingCasesGreaterThan(int count) {
        return repository.findByPendingCasesGreaterThan(count);
    }

    public List<Associate> findByOpenTasksGreaterThan(int count) {
        return repository.findByOpenTasksGreaterThan(count);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<Associate> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Associate> findByNamePaged(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<Associate> findByDesignationPaged(String designation, Pageable pageable) {
        return repository.findByDesignationContainingIgnoreCase(designation, pageable);
    }

    public Page<Associate> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }

    public Page<Associate> findByPendingCasesGreaterThanPaged(int count, Pageable pageable) {
        return repository.findByPendingCasesGreaterThan(count, pageable);
    }

    public Page<Associate> findByOpenTasksGreaterThanPaged(int count, Pageable pageable) {
        return repository.findByOpenTasksGreaterThan(count, pageable);
    }
}