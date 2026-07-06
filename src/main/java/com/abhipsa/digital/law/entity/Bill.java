package com.abhipsa.digital.law.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter @Setter
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String billNo;
    private LocalDate billDate;

    private double total;
    private double received;
    private double balance;

    @ManyToOne
    private CaseDetails caseDetails;
}