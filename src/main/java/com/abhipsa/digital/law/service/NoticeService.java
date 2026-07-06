package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository repository;

    public Notice create(Notice notice) {
        return repository.save(notice);
    }

    public List<Notice> getAll() {
        return repository.findAll();
    }

    public Notice getById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notice not found"));
    }

    public Notice update(String id, Notice notice) {

        Notice existing = getById(id);

        existing.setOfficeFileNo(notice.getOfficeFileNo());
        existing.setReferenceNo(notice.getReferenceNo());
        existing.setNoticeDate(notice.getNoticeDate());
        existing.setDispatchDate(notice.getDispatchDate());
        existing.setCaseDetails(notice.getCaseDetails());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public Notice findByReferenceNo(String referenceNo) {
        return repository.findByReferenceNo(referenceNo)
                .orElseThrow(() ->
                        new RuntimeException("Notice not found"));
    }

    public List<Notice> findByOfficeFileNo(String officeFileNo) {
        return repository.findByOfficeFileNoContainingIgnoreCase(officeFileNo);
    }

    public List<Notice> findByCaseId(String caseId) {
        return repository.findByCaseDetailsId(caseId);
    }

    public List<Notice> findByNoticeDate(LocalDate noticeDate) {
        return repository.findByNoticeDate(noticeDate);
    }

    public List<Notice> findByDispatchDate(LocalDate dispatchDate) {
        return repository.findByDispatchDate(dispatchDate);
    }

    public List<Notice> findByNoticeDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByNoticeDateBetween(start, end);
    }

    public List<Notice> findByDispatchDateBetween(
            LocalDate start,
            LocalDate end) {

        return repository.findByDispatchDateBetween(start, end);
    }

    public long countByCaseId(String caseId) {
        return repository.countByCaseDetailsId(caseId);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<Notice> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Notice> findByOfficeFileNoPaged(String officeFileNo, Pageable pageable) {
        return repository.findByOfficeFileNoContainingIgnoreCase(officeFileNo, pageable);
    }

    public Page<Notice> findByCaseIdPaged(String caseId, Pageable pageable) {
        return repository.findByCaseDetailsId(caseId, pageable);
    }

    public Page<Notice> findByNoticeDatePaged(LocalDate noticeDate, Pageable pageable) {
        return repository.findByNoticeDate(noticeDate, pageable);
    }

    public Page<Notice> findByDispatchDatePaged(LocalDate dispatchDate, Pageable pageable) {
        return repository.findByDispatchDate(dispatchDate, pageable);
    }

    public Page<Notice> findByNoticeDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        return repository.findByNoticeDateBetween(start, end, pageable);
    }

    public Page<Notice> findByDispatchDateBetweenPaged(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        return repository.findByDispatchDateBetween(start, end, pageable);
    }
}