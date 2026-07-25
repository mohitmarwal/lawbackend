package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.HearingDate;
import com.abhipsa.digital.law.repository.CaseDetailsRepository;
import com.abhipsa.digital.law.repository.HearingDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HearingDateService {

    private final HearingDateRepository repository;
    private final CaseDetailsRepository caseDetailsRepository;
    private final NotificationDispatchService notificationDispatchService;

    public HearingDate create(HearingDate hearingDate) {
        HearingDate saved = repository.save(hearingDate);
        notifyHearingEvent(saved, "New Hearing Date Submitted",
                "A new hearing entry has been submitted for case %s: " + orBlank(saved.getSummary())
                        + (saved.getNextDate() != null ? ". Next date: " + saved.getNextDate() + "." : "."));
        return saved;
    }

    public List<HearingDate> getAll() {
        return repository.findAll();
    }

    public HearingDate getById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Hearing date entry not found"));
    }

    public HearingDate update(String id, HearingDate hearingDate) {

        HearingDate existing = getById(id);

        hearingDate.setId(existing.getId());

        return repository.save(hearingDate);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    // Marks the entry approved; case-level nextDate/summary sync stays on
    // CaseDetailsService.approveCase, which is the flow lawreact actually uses.
    public HearingDate approve(String id) {
        HearingDate existing = getById(id);
        existing.setApproved(true);
        HearingDate saved = repository.save(existing);
        notifyHearingEvent(saved, "Hearing Date Approved",
                "The hearing entry for case %s has been approved: " + orBlank(saved.getSummary())
                        + (saved.getNextDate() != null ? ". Next date: " + saved.getNextDate() + "." : "."));
        return saved;
    }

    // The incoming/persisted relation is often just a stub ({id: "..."}), so
    // re-fetch the full case to get its case number for the message body.
    private void notifyHearingEvent(HearingDate hearingDate, String subjectPrefix, String bodyTemplate) {
        if (hearingDate.getCaseDetails() == null || hearingDate.getCaseDetails().getId() == null) {
            return;
        }
        caseDetailsRepository.findById(hearingDate.getCaseDetails().getId()).ifPresent(caseDetails ->
                notificationDispatchService.notifyCaseContacts(caseDetails,
                        subjectPrefix + " - " + caseDetails.getCaseNumber(),
                        bodyTemplate.formatted(caseDetails.getCaseNumber())));
    }

    private static String orBlank(String value) {
        return value == null ? "" : value;
    }

    public HearingDate updateSummary(String id, String summary) {
        HearingDate existing = getById(id);
        existing.setSummary(summary);
        return repository.save(existing);
    }

    public List<HearingDate> findByApproved(boolean approved) {
        return repository.findByApproved(approved);
    }

    public List<HearingDate> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<HearingDate> findByCourtId(String courtId) {
        return repository.findByCourtId(courtId);
    }

    public List<HearingDate> findBySubmittedBy(String userId) {
        return repository.findBySubmittedById(userId);
    }

    public List<HearingDate> findBySummary(String summary) {
        return repository.findBySummaryContainingIgnoreCase(summary);
    }

    public List<HearingDate> findByHearingDate(LocalDate hearingDate) {
        return repository.findByHearingDate(hearingDate);
    }

    public List<HearingDate> findByNextDate(LocalDate nextDate) {
        return repository.findByNextDate(nextDate);
    }

    public List<HearingDate> findByHearingDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByHearingDateBetween(start, end);
    }

    public List<HearingDate> findByNextDateBetween(
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

    public Page<HearingDate> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<HearingDate> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }

    public Page<HearingDate> findByApprovedPaged(boolean approved, Pageable pageable) {
        return repository.findByApproved(approved, pageable);
    }
}
