package com.abhipsa.digital.law.config;

// Holds which tenant (law firm) the current thread/request is acting for.
// Set by TenantResolutionFilter from the request's Host header subdomain,
// read by TenantRoutingDataSource to pick the right MySQL schema. Falls
// back to the default tenant (rather than null) so anything running outside
// an HTTP request - app startup, scheduled jobs - still resolves to a valid
// schema instead of crashing.
public class TenantContext {

    public static final String DEFAULT_TENANT = "default";

    private static final ThreadLocal<String> CURRENT = ThreadLocal.withInitial(() -> DEFAULT_TENANT);

    private TenantContext() {}

    public static String getCurrentTenant() {
        return CURRENT.get();
    }

    public static void setCurrentTenant(String slug) {
        CURRENT.set(slug != null ? slug : DEFAULT_TENANT);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
