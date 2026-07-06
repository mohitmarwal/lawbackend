package com.abhipsa.digital.law.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;


@Entity
@Getter @Setter
public class MobileContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String role;
    private String mobile;
    private String email;

    @ManyToOne
    private CaseDetails caseDetails;
}
