package com.abhipsa.digital.law.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Generic file attachment, usable by either a CaseDetails or a Notice (a
// row has exactly one of the two parent relations set). Notice-attached
// rows use "slot" to preserve its legacy single-document + single-receipt
// UX (re-upload to the same slot replaces the previous row); case-attached
// rows have no slot and simply accumulate, since a case can carry any
// number of documents.
@Entity
@Getter @Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String fileName;
    private String contentType;
    private Long sizeBytes;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;

    // Only meaningful when attached to a Notice: "document" or "receipt".
    private String slot;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "case_id")
    private CaseDetails caseDetails;

    @ManyToOne
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
}
