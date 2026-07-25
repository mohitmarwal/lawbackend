package com.abhipsa.digital.law.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Not persisted on this entity: the date the case's nextDate held
    // immediately before the most recent update, sourced from HearingDate
    // history and populated by CaseDetailsService at read time.
    @Transient
    private LocalDate previousDate;

    // ADD THESE

    private String caseType;

    private String matterType;

    private LocalDate limitationDate;

    // Not persisted directly: hydrated from plaintiffClient/defendantClient on
    // load (see hydratePartyNames), and resolved back into a Client relation
    // by the service on create/update. Keeps the plaintiff/defendant API
    // (JSON in and out) as plain strings while the schema stores a real FK.
    @Transient
    private String plaintiff;
    @Transient
    private String defendant;

    @ManyToOne
    @JoinColumn(name = "plaintiff_client_id")
    @JsonIgnore
    private Client plaintiffClient;

    @ManyToOne
    @JoinColumn(name = "defendant_client_id")
    @JsonIgnore
    private Client defendantClient;

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

    @PostLoad
    private void hydratePartyNames() {
        if (plaintiffClient != null) {
            plaintiff = plaintiffClient.getName();
        }
        if (defendantClient != null) {
            defendant = defendantClient.getName();
        }
    }
}