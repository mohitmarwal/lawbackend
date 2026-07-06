package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Approved;
import com.abhipsa.digital.law.repository.ApprovedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovedService {

    private final ApprovedRepository repository;

    public Approved create(Approved approved) {
        return repository.save(approved);
    }

    public List<Approved> getAll() {
        return repository.findAll();
    }

    public Approved getById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Approved record not found"));
    }

    public Approved update(String id, Approved approved) {

        Approved existing = getById(id);

        approved.setId(existing.getId());

        return repository.save(approved);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<Approved> findByApproved(boolean approved) {
        return repository.findByApproved(approved);
    }

    public List<Approved> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<Approved> findByCourtId(String courtId) {
        return repository.findByCourtId(courtId);
    }

    public List<Approved> findByEnteredBy(String enteredBy) {
        return repository.findByEnteredByContainingIgnoreCase(enteredBy);
    }

    public List<Approved> findBySummary(String summary) {
        return repository.findBySummaryContainingIgnoreCase(summary);
    }

    public List<Approved> findByHearingDate(LocalDate hearingDate) {
        return repository.findByHearingDate(hearingDate);
    }

    public List<Approved> findByNextDate(LocalDate nextDate) {
        return repository.findByNextDate(nextDate);
    }

    public List<Approved> findByHearingDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByHearingDateBetween(start, end);
    }

    public List<Approved> findByNextDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByNextDateBetween(start, end);
    }

    public long countApproved() {
        return repository.countByApproved(true);
    }

    public long countPendingApproval() {
        return repository.countByApproved(false);
    }

    public boolean existsByCaseId(String caseId) {
        return repository.existsByCaseDetailsId(caseId);
    }
}