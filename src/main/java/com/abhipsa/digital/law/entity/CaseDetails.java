package com.abhipsa.digital.law.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Entity
@Getter
@Setter
public class CaseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Transient // Tells Hibernate not to look for these columns in the case table itself
    private String contactName;
    @Transient
    private String contactEmail;
    @Transient
    private List<String> whatsappNumbers;
    private String caseNumber;
    private String officeFileNumber;

    private LocalDate filingDate;
    private LocalDate nextDate;

    // ADD THESE

    private String caseType;

    private String matterType;

    private LocalDate limitationDate;

    private String plaintiff;
    private String defendant;

    private String description;
    private String status;
    // In CaseDetails.java
    private Boolean approved = false;   // default false


    @Column(name = "approved_on")
    private LocalDate approvedOn;


    @ManyToOne
    @JoinColumn(name = "court_id")
    private Court court;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;
}