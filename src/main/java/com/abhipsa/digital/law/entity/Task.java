package com.abhipsa.digital.law.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter @Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String task;
    private String type;
    private String priority;
    private String status;

    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private CaseDetails caseDetails;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
}
