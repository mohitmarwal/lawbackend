package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.TenantBranding;
import com.abhipsa.digital.law.service.TenantBrandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// Authenticated read/write of the current tenant's own branding (firm name,
// logo). PUT is admin-only (see SecurityConfig) - viewing it is fine for any
// logged-in user (the sidebar needs it), changing it isn't.
@RestController
@RequestMapping("/api/branding")
@RequiredArgsConstructor
public class TenantBrandingController {

    private final TenantBrandingService service;

    @GetMapping
    public TenantBranding getLatest() {
        return service.getLatest();
    }

    @PutMapping
    public TenantBranding update(
            @RequestParam(required = false) String firmName,
            @RequestParam(required = false) String headerTagline,
            @RequestParam(required = false) MultipartFile logo) throws IOException {

        return service.update(
                firmName,
                headerTagline,
                logo != null ? logo.getContentType() : null,
                logo != null ? logo.getBytes() : null);
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
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(branding.getLogoData());
    }
}
