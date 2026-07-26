package com.abhipsa.digital.law.entity;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Getter @Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String actor;
    private String entityType;
    private String entityId;
    private String action;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String diffJson;

    private LocalDateTime createdAt;
}
