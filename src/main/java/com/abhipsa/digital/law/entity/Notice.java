package com.abhipsa.digital.law.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter @Setter
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String officeFileNo;
    private String referenceNo;

    private LocalDate noticeDate;
    private LocalDate dispatchDate;

    @ManyToOne
    private CaseDetails caseDetails;
    private String courierName;
    private String trackingNumber;
    private String deliveryStatus; // DELIVERED, UNDELIVERED, PENDING
    private String suitStatus;

    private String description;

    // Comma-separated paths/URLs of uploaded notice documents.
    private String documents;

    private LocalDate limitationBegins;

    private boolean approved = false;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private LocalDate approvedOn;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    // Which party our client is on for this notice: "Plaintiff" or "Defendant".
    private String clientSide;

    private LocalDate deliveredOn;

    private String documentName;
    private String documentContentType;
    private Long documentSizeBytes;

    // Excluded from normal JSON responses (list/detail) so notice listings
    // don't inline megabytes of base64 per row; served only via the
    // dedicated /document download endpoint.
    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] documentData;

    private String receiptName;
    private String receiptContentType;
    private Long receiptSizeBytes;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] receiptData;
}

