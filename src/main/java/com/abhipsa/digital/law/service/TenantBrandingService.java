package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.TenantBranding;
import com.abhipsa.digital.law.repository.TenantBrandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantBrandingService {

    private final TenantBrandingRepository repository;

    // One row per tenant schema; falls back to the "Matterly" product name
    // until a firm customizes it via update() below.
    public TenantBranding getLatest() {
        List<TenantBranding> all = repository.findAllByOrderByUpdatedAtDesc();
        if (!all.isEmpty()) {
            return all.get(0);
        }
        TenantBranding blank = new TenantBranding();
        blank.setFirmName("Matterly");
        return repository.save(blank);
    }

    public TenantBranding update(String firmName, String headerTagline, String logoContentType, byte[] logoData) {
        TenantBranding latest = getLatest();
        if (firmName != null && !firmName.isBlank()) {
            latest.setFirmName(firmName);
        }
        latest.setHeaderTagline(headerTagline);
        if (logoData != null && logoData.length > 0) {
            latest.setLogoData(logoData);
            latest.setLogoContentType(logoContentType);
        }
        latest.setUpdatedAt(LocalDateTime.now());
        return repository.save(latest);
    }
}
