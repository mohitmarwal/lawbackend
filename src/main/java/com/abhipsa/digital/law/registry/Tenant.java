package com.abhipsa.digital.law.registry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Lives in the separate, always-connected "tenant_registry" schema (see
// RegistryDataSourceConfig) - deliberately NOT part of the main tenant-
// routed entity set, since resolving which tenant schema to route to is
// exactly the problem this table solves; it can't itself be tenant-routed.
//
// Just the slug -> schema mapping - firm name/logo live in TenantBranding,
// inside each tenant's own schema. Tenant resolution is Host-header-based
// (see TenantResolutionFilter), not JWT-based, so that schema is reachable
// even pre-login; there's no need to duplicate branding data here.
@Entity
@Getter @Setter
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String slug;

    private String schemaName;

    private LocalDateTime createdAt = LocalDateTime.now();
}
