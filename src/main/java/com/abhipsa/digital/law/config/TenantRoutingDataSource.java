package com.abhipsa.digital.law.config;

import com.abhipsa.digital.law.registry.Tenant;
import com.abhipsa.digital.law.registry.TenantRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// This IS the app's primary DataSource bean (see TenantDataSourceConfig) -
// every existing @Entity/JpaRepository keeps working completely unchanged,
// unaware that getConnection() is actually being routed per-request to a
// different tenant's MySQL schema.
//
// Deliberately overrides determineTargetDataSource() entirely rather than
// relying on AbstractRoutingDataSource's built-in fixed target-map: that
// map is only populated once at startup, which can't satisfy "a brand new
// tenant works immediately, no restart." Instead this keeps its own
// self-populating cache, built lazily on first use per tenant.
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<String, DataSource> cache = new ConcurrentHashMap<>();
    private final TenantRepository tenantRepository;
    private final String jdbcUrlTemplate;
    private final String username;
    private final String password;
    private final String defaultSchema;

    public TenantRoutingDataSource(TenantRepository tenantRepository, String jdbcUrlTemplate,
                                    String username, String password, String defaultSchema) {
        this.tenantRepository = tenantRepository;
        this.jdbcUrlTemplate = jdbcUrlTemplate;
        this.username = username;
        this.password = password;
        this.defaultSchema = defaultSchema;
        // Required by the base class even though determineTargetDataSource()
        // below is fully overridden and never consults these.
        setTargetDataSources(Map.of());
        setDefaultTargetDataSource(new HikariDataSource());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String slug = TenantContext.getCurrentTenant();
        return cache.computeIfAbsent(slug, this::buildDataSourceForSlug);
    }

    private DataSource buildDataSourceForSlug(String slug) {
        String schemaName = TenantContext.DEFAULT_TENANT.equals(slug)
                ? defaultSchema
                : tenantRepository.findBySlug(slug)
                        .map(Tenant::getSchemaName)
                        .orElseThrow(() -> new IllegalStateException("Unknown tenant: " + slug));

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrlTemplate.replace("{schema}", schemaName));
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        // Small pool per tenant - this is one of potentially many schemas
        // sharing the same MySQL server's connection budget.
        dataSource.setMaximumPoolSize(5);
        dataSource.setPoolName("tenant-" + slug);
        return dataSource;
    }
}
