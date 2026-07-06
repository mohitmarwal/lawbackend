package com.abhipsa.digital.law.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Approved {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDate hearingDate;

    private LocalDate nextDate;

    @Column(length = 5000)
    private String summary;

    private String enteredBy;

    private boolean approved;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private CaseDetails caseDetails;

    @ManyToOne
    @JoinColumn(name = "court_id")
    private Court court;
}