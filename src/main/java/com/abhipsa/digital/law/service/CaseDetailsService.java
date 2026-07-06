package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.MobileContact;
import com.abhipsa.digital.law.repository.CaseDetailsRepository;
import com.abhipsa.digital.law.repository.MobileContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
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
    // 1. Inject EntityManager at the top of your class
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public CaseDetails create(CaseDetails caseDetails) {
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
        return repository.findDailyBoardData(date);
    }

    public  long countFiledSuitComplaint() {
        return repository.countFiledSuitComplaint();
    }

    public void approveCase(String id) {
        CaseDetails caseDetails = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        caseDetails.setApproved(true);
        caseDetails.setApprovedOn(LocalDate.now());   // Set today's date
        repository.save(caseDetails);
    }

    public long countLimitationBegins() {

        LocalDate today = LocalDate.now();
        LocalDate next30Days = today.plusDays(30);

        return repository.countByLimitationDateBetween(
                today,
                next30Days);
    }

    public List<CaseDetails> getAll() {
        return repository.findAll();
    }

    public CaseDetails getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));
    }

    public CaseDetails update(String id, CaseDetails caseDetails) {

        CaseDetails existing = getById(id);

        existing.setCaseNumber(caseDetails.getCaseNumber());
        existing.setOfficeFileNumber(caseDetails.getOfficeFileNumber());
        existing.setFilingDate(caseDetails.getFilingDate());
        existing.setNextDate(caseDetails.getNextDate());
        existing.setPlaintiff(caseDetails.getPlaintiff());
        existing.setDefendant(caseDetails.getDefendant());
        existing.setDescription(caseDetails.getDescription());
        existing.setStatus(caseDetails.getStatus());
        existing.setCourt(caseDetails.getCourt());
        existing.setAssignedUser(caseDetails.getAssignedUser());

        return repository.save(existing);
    }

    public void delete(String id) {
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
        return repository.findByStatus(status);
    }

    public List<CaseDetails> findByCourtId(String courtId) {
        return repository.findByCourtId(courtId);
    }

    public List<CaseDetails> findByAssignedUserId(String userId) {
        return repository.findByAssignedUserId(userId);
    }

    public List<CaseDetails> findByPlaintiff(String plaintiff) {
        return repository.findByPlaintiffContainingIgnoreCase(plaintiff);
    }

    public List<CaseDetails> findByDefendant(String defendant) {
        return repository.findByDefendantContainingIgnoreCase(defendant);
    }

    public List<CaseDetails> findByFilingDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByFilingDateBetween(start, end);
    }

    public List<CaseDetails> findByNextDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByNextDateBetween(start, end);
    }

    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<CaseDetails> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<CaseDetails> findByStatusPaged(String status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    public Page<CaseDetails> findByCourtIdPaged(String courtId, Pageable pageable) {
        return repository.findByCourtId(courtId, pageable);
    }

    public Page<CaseDetails> findByAssignedUserIdPaged(String userId, Pageable pageable) {
        return repository.findByAssignedUserId(userId, pageable);
    }

    public Page<CaseDetails> findByPlaintiffPaged(String plaintiff, Pageable pageable) {
        return repository.findByPlaintiffContainingIgnoreCase(plaintiff, pageable);
    }

    public Page<CaseDetails> findByDefendantPaged(String defendant, Pageable pageable) {
        return repository.findByDefendantContainingIgnoreCase(defendant, pageable);
    }

    public Page<CaseDetails> findByNextDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        return repository.findByNextDateBetween(start, end, pageable);
    }

    public Page<CaseDetails> findByFilingDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        return repository.findByFilingDateBetween(start, end, pageable);
    }
}