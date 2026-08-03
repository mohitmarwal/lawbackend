package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.TenantBranding;
import com.abhipsa.digital.law.service.TenantBrandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// No-auth branding lookup for pre-login pages (landing page, login screen).
// Tenant is already resolved from the Host header by TenantResolutionFilter
// before this runs (see SecurityConfig, which permits /api/public/**), so
// this reads the exact same tenant-routed TenantBranding row as the
// authenticated controller - just without requiring a session.
@RestController
@RequestMapping("/api/public/branding")
@RequiredArgsConstructor
public class PublicBrandingController {

    private final TenantBrandingService service;

    @GetMapping
    public PublicBrandingResponse get() {
        TenantBranding branding = service.getLatest();
        return new PublicBrandingResponse(
                branding.getFirmName(),
                branding.getHeaderTagline(),
                branding.getLogoData() != null);
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> logo() {
        TenantBranding branding = service.getLatest();
        if (branding.getLogoData() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = branding.getLogoContentType() != null
                ? MediaType.parseMediaType(branding.getLogoContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .body(branding.getLogoData());
    }

    public record PublicBrandingResponse(String firmName, String headerTagline, boolean hasLogo) {
    }
}
