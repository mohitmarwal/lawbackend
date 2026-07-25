package com.abhipsa.digital.law.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class HearingDate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDate hearingDate;

    private LocalDate nextDate;

    @Column(length = 5000)
    private String summary;

    private boolean approved;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "case_id")
    private CaseDetails caseDetails;

    @ManyToOne
    @JoinColumn(name = "court_id")
    private Court court;

    @ManyToOne
    @JoinColumn(name = "submitted_by")
    private User submittedBy;
}
