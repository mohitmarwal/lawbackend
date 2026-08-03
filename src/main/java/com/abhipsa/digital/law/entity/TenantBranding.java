package com.abhipsa.digital.law.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// One row per tenant schema (same "latest row wins" singleton convention as
// TermsAndConditions) - the firm name/logo shown in place of "Matterly"
// everywhere: sidebar, landing page, invoice letterhead, notifications.
// Tenant-routed like every other entity here, so it's automatically scoped
// to whichever schema the current request resolved to.
@Entity
@Getter
@Setter
public class TenantBranding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String firmName;

    private String headerTagline;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] logoData;

    private String logoContentType;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
