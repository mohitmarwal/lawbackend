package com.abhipsa.digital.law.entity;


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
}

