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

    private LocalDateTime sentAt;
    private String status;

    @ManyToOne
    private CaseDetails caseDetails;
}