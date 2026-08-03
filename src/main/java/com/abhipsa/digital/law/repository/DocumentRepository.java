package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    List<Document> findByCaseDetailsIdOrderByUploadedAtDesc(String caseId);

    List<Document> findByNoticeIdOrderByUploadedAtDesc(String noticeId);

    Optional<Document> findByNoticeIdAndSlot(String noticeId, String slot);

    // Batch lookup used to enrich a page of notices with their document/
    // receipt metadata in one query instead of one per row.
    List<Document> findByNoticeIdIn(List<String> noticeIds);
}
