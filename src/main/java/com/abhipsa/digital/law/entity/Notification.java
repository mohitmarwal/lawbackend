package com.abhipsa.digital.law.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String referenceNo;
    private String channel;
    private String message;
    private String recipient;

    private LocalDateTime sentAt;
    private String status; // pending | sent | failed
    private int retryCount = 0;

    @ManyToOne
    private CaseDetails caseDetails;
}