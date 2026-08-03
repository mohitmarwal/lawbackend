package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.HearingDate;
import com.abhipsa.digital.law.entity.MobileContact;
import com.abhipsa.digital.law.repository.CaseDetailsRepository;
import com.abhipsa.digital.law.repository.HearingDateRepository;
import com.abhipsa.digital.law.repository.MobileContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaseDetailsService {

    @Autowired
    private  CaseDetailsRepository repository;
    @Autowired
    private  MobileContactRepository mobile;
    @Autowired
    private  ClientService clientService;
    @Autowired
    private  NotificationDispatchService notificationDispatchService;
    @Autowired
    private  HearingDateRepository hearingDateRepository;
    @Autowired
    private  CurrentUserService currentUserService;
    // 1. Inject EntityManager at the top of your class
    @PersistenceContext
    private EntityManager entityManager;

    // Non-admins (associate, senior_associate) only ever see/act on cases
    // assigned to them; null means "no restriction" (admin).
    private String scopeUserId() {
        return currentUserService.isAdmin() ? null : currentUserService.getUserId();
    }

    private void assertOwnership(CaseDetails caseDetails) {
        if (currentUserService.isAdmin()) return;
        if (currentUserService.isClient()) {
            String clientId = currentUserService.getClientId();
            String plaintiffId = caseDetails.getPlaintiffClient() != null ? caseDetails.getPlaintiffClient().getId() : null;
            String defendantId = caseDetails.getDefendantClient() != null ? caseDetails.getDefendantClient().getId() : null;
            if (clientId != null && (clientId.equals(plaintiffId) || clientId.equals(defendantId))) return;
            throw new AccessDeniedException("This case is not linked to your account");
        }
        String myId = currentUserService.getUserId();
        String assignedId = caseDetails.getAssignedUser() != null ? caseDetails.getAssignedUser().getId() : null;
        if (myId == null || !myId.equals(assignedId)) {
            throw new AccessDeniedException("This case is not assigned to you");
        }
    }

    // Populates the transient previousDate field from HearingDate history:
    // for each case, the hearingDate of its most recent HearingDate row (the
    // date that was "next" immediately before the current nextDate).
    private void enrichPreviousDates(List<CaseDetails> cases) {
        if (cases.isEmpty()) return;
        List<String> ids = cases.stream().map(CaseDetails::getId).toList();
        List<HearingDate> history = hearingDateRepository.findByCaseDetailsIdInOrderByHearingDateDesc(ids);

        Map<String, LocalDate> latestByCase = new HashMap<>();
        for (HearingDate hd : history) {
            if (hd.getCaseDetails() == null || hd.getCaseDetails().getId() == null) continue;
            String caseId = hd.getCaseDetails().getId();
            // History is ordered by hearingDate desc, so the first entry seen
            // per case id is already the most recent one.
            latestByCase.putIfAbsent(caseId, hd.getHearingDate());
        }

        for (CaseDetails c : cases) {
            c.setPreviousDate(latestByCase.get(c.getId()));
        }
    }

    private CaseDetails enrichPreviousDate(CaseDetails caseDetails) {
        if (caseDetails != null) {
            enrichPreviousDates(List.of(caseDetails));
        }
        return caseDetails;
    }

    @Transactional
    public CaseDetails create(CaseDetails caseDetails) {
        if (currentUserService.isClient()) {
            throw new AccessDeniedException("Clients cannot create cases");
        }
        if (!currentUserService.isAdmin()) {
            // Non-admins can only ever create a case assigned to themselves,
            // regardless of what the "Assigned Associate" field said.
            caseDetails.setAssignedUser(currentUserService.getUser());
        }
        caseDetails.setPlaintiffClient(clientService.resolveByName(caseDetails.getPlaintiff()));
        caseDetails.setDefendantClient(clientService.resolveByName(caseDetails.getDefendant()));

        // 1. Save the main Case first
        CaseDetails savedCase = repository.save(caseDetails);

        if (caseDetails.getWhatsappNumbers() != null) {
            for (String mobileNum : caseDetails.getWhatsappNumbers()) {
                if (mobileNum == null || mobileNum.trim().isEmpty()) continue;

                String cleanedMobile = mobileNum.trim();

                // 2. Perform a direct update/insert using your repository
                // Use a custom method in your MobileContactRepository
                // that uses an @Modifying @Query to bypass the entity manager cache.
                mobile.upsertContact(
                        cleanedMobile,
                        caseDetails.getContactName(),
                        caseDetails.getContactEmail(),
                        "CLIENT",
                        savedCase.getId() // Passing the ID of the newly saved Case
                );

            }
        }
        return savedCase;
    }

    public List<Map<String, Object>> getDailyBoardData(LocalDate date) {
        // The service calls the repository and returns the flat Map list
        // which Spring Boot will automatically serialize to JSON
        return repository.findDailyBoardData(date, scopeUserId());
    }

    // The daily-board query is a native SQL projection (not a JPA entity), so
    // it can't use Spring Data's Page<T> derivation directly; paginate the
    // already-filtered (single day) result in memory instead.
    public Page<Map<String, Object>> getDailyBoardDataPaged(LocalDate date, Pageable pageable) {
        List<Map<String, Object>> all = repository.findDailyBoardData(date, scopeUserId());

        int start = (int) pageable.getOffset();
        if (start >= all.size()) {
            return new PageImpl<>(List.of(), pageable, all.size());
        }
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    public  long countFiledSuitComplaint() {
        return repository.countFiledSuitComplaint();
    }

    public void approveCase(String id) {
        CaseDetails caseDetails = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        caseDetails.setApproved(true);
        caseDetails.setApprovedOn(LocalDate.now());   // Set today's date
        CaseDetails saved = repository.save(caseDetails);

        notificationDispatchService.notifyCaseContacts(saved,
                "Case Update Approved - " + saved.getCaseNumber(),
                "The latest hearing update for case " + saved.getCaseNumber() + " has been approved."
                        + (saved.getNextDate() != null ? " Next date: " + saved.getNextDate() + "." : ""));
    }

    public long countLimitationBegins() {

        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);

        return repository.countByLimitationDateBetween(
                today,
                next30Days);
    }

    public List<CaseDetails> getAll() {
        if (currentUserService.isClient()) {
            String clientId = currentUserService.getClientId();
            List<CaseDetails> clientCases = repository.findByPlaintiffClientIdOrDefendantClientId(clientId, clientId);
            enrichPreviousDates(clientCases);
            return clientCases;
        }
        String myId = scopeUserId();
        List<CaseDetails> all = myId == null ? repository.findAll() : repository.findByAssignedUserId(myId);
        enrichPreviousDates(all);
        return all;
    }

    public CaseDetails getById(String id) {
        CaseDetails caseDetails = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        assertOwnership(caseDetails);
        return enrichPreviousDate(caseDetails);
    }

    public CaseDetails update(String id, CaseDetails caseDetails) {
        if (currentUserService.isClient()) {
            throw new AccessDeniedException("Clients cannot edit case details");
        }

        CaseDetails existing = getById(id);

        String previousStatus = existing.getStatus();
        LocalDate previousNextDate = existing.getNextDate();

        existing.setCaseNumber(caseDetails.getCaseNumber());
        existing.setOfficeFileNumber(caseDetails.getOfficeFileNumber());
        existing.setFilingDate(caseDetails.getFilingDate());
        existing.setNextDate(caseDetails.getNextDate());
        existing.setPlaintiff(caseDetails.getPlaintiff());
        existing.setDefendant(caseDetails.getDefendant());
        existing.setPlaintiffClient(clientService.resolveByName(caseDetails.getPlaintiff()));
        existing.setDefendantClient(clientService.resolveByName(caseDetails.getDefendant()));
        existing.setDescription(caseDetails.getDescription());
        existing.setStatus(caseDetails.getStatus());
        existing.setCourt(caseDetails.getCourt());
        // Only an admin can reassign a case to a different associate; a
        // non-admin's own edits can't move the case off themselves.
        if (currentUserService.isAdmin()) {
            existing.setAssignedUser(caseDetails.getAssignedUser());
        }

        CaseDetails saved = repository.save(existing);

        boolean statusChanged = saved.getStatus() != null && !saved.getStatus().equals(previousStatus);
        boolean nextDateChanged = saved.getNextDate() != null && !saved.getNextDate().equals(previousNextDate);

        // Log the date that's being replaced into hearing-date history before
        // it's lost, so "previous date" can be looked up later.
        if (nextDateChanged && previousNextDate != null) {
            HearingDate history = new HearingDate();
            history.setCaseDetails(saved);
            history.setHearingDate(previousNextDate);
            history.setNextDate(saved.getNextDate());
            history.setApproved(true);
            history.setSummary("Next hearing date updated from " + previousNextDate + " to " + saved.getNextDate() + ".");
            hearingDateRepository.save(history);
        }

        if (statusChanged) {
            notificationDispatchService.notifyCaseContacts(saved,
                    "Case Status Updated - " + saved.getCaseNumber(),
                    "The status of case " + saved.getCaseNumber() + " has been updated to " + saved.getStatus() + ".");
        }
        if (nextDateChanged) {
            notificationDispatchService.notifyCaseContacts(saved,
                    "Hearing Date Updated - " + saved.getCaseNumber(),
                    "The next hearing date for case " + saved.getCaseNumber() + " has been updated to " + saved.getNextDate() + ".");
        }

        return enrichPreviousDate(saved);
    }

    public void delete(String id) {
        if (currentUserService.isClient()) {
            throw new AccessDeniedException("Clients cannot delete cases");
        }
        repository.deleteById(id);
    }

    public CaseDetails findByCaseNumber(String caseNumber) {
        return repository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RuntimeException("Case not found"));
    }

    public CaseDetails findByOfficeFileNumber(String officeFileNumber) {
        return repository.findByOfficeFileNumber(officeFileNumber)
                .orElseThrow(() -> new RuntimeException("Case not found"));
    }

    public List<CaseDetails> findByStatus(String status) {
        String myId = scopeUserId();
        List<CaseDetails> results = myId == null
                ? repository.findByStatus(status)
                : repository.findByStatusAndAssignedUserId(status, myId);
        enrichPreviousDates(results);
        return results;
    }

    public List<CaseDetails> findByCourtId(String courtId) {
        String myId = scopeUserId();
        List<CaseDetails> results = myId == null
                ? repository.findByCourtId(courtId)
                : repository.findByCourtIdAndAssignedUserId(courtId, myId);
        enrichPreviousDates(results);
        return results;
    }

    // Non-admins can only ever query their own assignment, regardless of
    // which userId they pass in.
    public List<CaseDetails> findByAssignedUserId(String userId) {
        String myId = scopeUserId();
        List<CaseDetails> results = repository.findByAssignedUserId(myId != null ? myId : userId);
        enrichPreviousDates(results);
        return results;
    }

    public List<CaseDetails> findByPlaintiff(String plaintiff) {
        String myId = scopeUserId();
        List<CaseDetails> results = myId == null
                ? repository.findByPlaintiffClient_NameContainingIgnoreCase(plaintiff)
                : repository.findByPlaintiffClient_NameContainingIgnoreCaseAndAssignedUserId(plaintiff, myId);
        enrichPreviousDates(results);
        return results;
    }

    public List<CaseDetails> findByDefendant(String defendant) {
        String myId = scopeUserId();
        List<CaseDetails> results = myId == null
                ? repository.findByDefendantClient_NameContainingIgnoreCase(defendant)
                : repository.findByDefendantClient_NameContainingIgnoreCaseAndAssignedUserId(defendant, myId);
        enrichPreviousDates(results);
        return results;
    }

    public List<CaseDetails> findByFilingDateBetween(
            LocalDate start,
            LocalDate end) {

        String myId = scopeUserId();
        List<CaseDetails> results = myId == null
                ? repository.findByFilingDateBetween(start, end)
                : repository.findByFilingDateBetweenAndAssignedUserId(start, end, myId);
        enrichPreviousDates(results);
        return results;
    }

    public List<CaseDetails> findByNextDateBetween(
            LocalDate start,
            LocalDate end) {

        String myId = scopeUserId();
        List<CaseDetails> results = myId == null
                ? repository.findByNextDateBetween(start, end)
                : repository.findByNextDateBetweenAndAssignedUserId(start, end, myId);
        enrichPreviousDates(results);
        return results;
    }

    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<CaseDetails> getAllPaged(Pageable pageable) {
        if (currentUserService.isClient()) {
            String clientId = currentUserService.getClientId();
            Page<CaseDetails> clientPage = repository.findByPlaintiffClientIdOrDefendantClientId(clientId, clientId, pageable);
            enrichPreviousDates(clientPage.getContent());
            return clientPage;
        }
        String myId = scopeUserId();
        Page<CaseDetails> page = myId == null ? repository.findAll(pageable) : repository.findByAssignedUserId(myId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }

    public Page<CaseDetails> findByStatusPaged(String status, Pageable pageable) {
        String myId = scopeUserId();
        Page<CaseDetails> page = myId == null
                ? repository.findByStatus(status, pageable)
                : repository.findByStatusAndAssignedUserId(status, myId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }

    public Page<CaseDetails> findByCourtIdPaged(String courtId, Pageable pageable) {
        String myId = scopeUserId();
        Page<CaseDetails> page = myId == null
                ? repository.findByCourtId(courtId, pageable)
                : repository.findByCourtIdAndAssignedUserId(courtId, myId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }

    // Non-admins can only ever query their own assignment, regardless of
    // which userId they pass in.
    public Page<CaseDetails> findByAssignedUserIdPaged(String userId, Pageable pageable) {
        String myId = scopeUserId();
        Page<CaseDetails> page = repository.findByAssignedUserId(myId != null ? myId : userId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }

    public Page<CaseDetails> findByPlaintiffPaged(String plaintiff, Pageable pageable) {
        String myId = scopeUserId();
        Page<CaseDetails> page = myId == null
                ? repository.findByPlaintiffClient_NameContainingIgnoreCase(plaintiff, pageable)
                : repository.findByPlaintiffClient_NameContainingIgnoreCaseAndAssignedUserId(plaintiff, myId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }

    public Page<CaseDetails> findByDefendantPaged(String defendant, Pageable pageable) {
        String myId = scopeUserId();
        Page<CaseDetails> page = myId == null
                ? repository.findByDefendantClient_NameContainingIgnoreCase(defendant, pageable)
                : repository.findByDefendantClient_NameContainingIgnoreCaseAndAssignedUserId(defendant, myId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }

    public Page<CaseDetails> findByNextDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        String myId = scopeUserId();
        Page<CaseDetails> page = myId == null
                ? repository.findByNextDateBetween(start, end, pageable)
                : repository.findByNextDateBetweenAndAssignedUserId(start, end, myId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }

    public Page<CaseDetails> findByFilingDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        String myId = scopeUserId();
        Page<CaseDetails> page = myId == null
                ? repository.findByFilingDateBetween(start, end, pageable)
                : repository.findByFilingDateBetweenAndAssignedUserId(start, end, myId, pageable);
        enrichPreviousDates(page.getContent());
        return page;
    }
}