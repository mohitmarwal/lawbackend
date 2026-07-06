package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.MobileContact;
import com.abhipsa.digital.law.repository.MobileContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MobileContactService {

    private final MobileContactRepository repository;

    public MobileContact create(MobileContact mobileContact) {
        return repository.save(mobileContact);
    }

    public List<MobileContact> getAll() {
        return repository.findAll();
    }

    public MobileContact getById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mobile Contact not found"));
    }

    public MobileContact update(String id, MobileContact mobileContact) {

        MobileContact existing = getById(id);

        existing.setName(mobileContact.getName());
        existing.setRole(mobileContact.getRole());
        existing.setMobile(mobileContact.getMobile());
        existing.setCaseDetails(mobileContact.getCaseDetails());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public MobileContact findByMobile(String mobile) {
        return repository.findByMobile(mobile)
                .orElseThrow(() ->
                        new RuntimeException("Mobile Contact not found"));
    }

    public List<MobileContact> findByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public List<MobileContact> findByRole(String role) {
        return repository.findByRoleContainingIgnoreCase(role);
    }

    public List<MobileContact> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public long countByRole(String role) {
        return repository.countByRole(role);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<MobileContact> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<MobileContact> findByNamePaged(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<MobileContact> findByRolePaged(String role, Pageable pageable) {
        return repository.findByRoleContainingIgnoreCase(role, pageable);
    }

    public Page<MobileContact> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }
}