package com.abhipsa.digital.law.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;


@Entity
@Getter @Setter
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String designation;
    private String mobile;
    private String email;

    private int pendingCases;
    private int openTasks;

    @ManyToOne
    private CaseDetails caseDetails;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}

