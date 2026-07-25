package com.abhipsa.digital.law.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Entity
@Getter @Setter
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String billNo;

    // Links a retainer bill back to the previous bill in its series.
    private String previousBillNo;

    private LocalDate billDate;

    private double total;
    private double received;
    private double balance;

    // Per-bill override of the global TermsAndConditions template.
    @Column(length = 5000)
    private String termsAndConditions;

    @ManyToOne
    private CaseDetails caseDetails;

    // Optional: a bill can be a flat total (as lawreact creates them today)
    // or itemized via BillLineItem, in which case calculateTotal() sums these.
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillLineItem> lineItems;
}