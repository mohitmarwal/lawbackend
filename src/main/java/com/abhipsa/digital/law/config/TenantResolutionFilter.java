package com.abhipsa.digital.law.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

// Resolves which tenant (law firm) a request belongs to from the Host
// header's subdomain (e.g. "acme.matterly.in" -> slug "acme") and sets it
// into TenantContext for the rest of the request - see SecurityConfig,
// where this is wired to run before JwtAuthFilter, since a tenant's own
// users table lives inside that tenant's schema and can't be queried until
// the tenant is already known. Cleared in a finally block so thread-pooled
// request threads never leak a tenant into the next, unrelated request.
//
// Local dev, bare IPs, the bare base domain, and any unrecognized host
// (e.g. a raw minikube port-forward hostname) all resolve to the "default"
// tenant - the pre-existing law_db schema - so nothing about today's local
// setup needs a hosts-file entry to keep working.
@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    @Value("${app.tenant.base-domain:matterly.in}")
    private String baseDomain;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            TenantContext.setCurrentTenant(resolveSlug(request.getServerName()));
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveSlug(String host) {
        if (host == null) {
            return TenantContext.DEFAULT_TENANT;
        }
        String normalized = host.toLowerCase(Locale.ROOT);

        if (normalized.equals("localhost") || normalized.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            return TenantContext.DEFAULT_TENANT;
        }

        String base = baseDomain.toLowerCase(Locale.ROOT);
        if (normalized.equals(base) || normalized.equals("www." + base)) {
            return TenantContext.DEFAULT_TENANT;
        }

        String suffix = "." + base;
        if (normalized.endsWith(suffix)) {
            String slug = normalized.substring(0, normalized.length() - suffix.length());
            return slug.isEmpty() ? TenantContext.DEFAULT_TENANT : slug;
        }

        return TenantContext.DEFAULT_TENANT;
    }
}
