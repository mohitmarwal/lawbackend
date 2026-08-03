package com.abhipsa.digital.law.config;

import com.abhipsa.digital.law.registry.TenantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// Wires the tenant-routing DataSource in as the app's primary DataSource,
// and - since Spring Boot's JPA autoconfiguration only auto-builds the
// default entityManagerFactory/transactionManager for the single-DataSource
// case - manually builds those two as well (Spring's own documented pattern
// once a second DataSource/EntityManagerFactory exists anywhere in the
// context, e.g. RegistryDataSourceConfig). Named exactly
// "entityManagerFactory"/"transactionManager" so every existing
// @EnableJpaRepositories/@Transactional usage (which defaults to those
// names) needs no changes.
//
// Built via LocalContainerEntityManagerFactoryBean directly rather than the
// autoconfigured EntityManagerFactoryBuilder helper: that shared builder
// bean itself requires "a" DataSource to construct, and since this
// DataSource bean depends on the registry's EntityManagerFactory (to look
// up tenants), routing through the shared builder created a genuine
// circular dependency between the two EntityManagerFactories.
@Configuration
public class TenantDataSourceConfig {

    @Value("${app.tenant.jdbc-url-template}")
    private String jdbcUrlTemplate;

    @Value("${app.tenant.datasource-username}")
    private String username;

    @Value("${app.tenant.datasource-password}")
    private String password;

    @Value("${app.tenant.default-schema}")
    private String defaultSchema;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    @Value("${spring.jpa.database-platform}")
    private String dialect;

    @Value("${spring.jpa.show-sql}")
    private String showSql;

    @Value("${spring.jpa.properties.hibernate.format_sql}")
    private String formatSql;

    @Value("${spring.jpa.properties.hibernate.session_factory.interceptor}")
    private String auditInterceptor;

    @Bean
    @Primary
    public DataSource dataSource(TenantRepository tenantRepository) {
        return new TenantRoutingDataSource(tenantRepository, jdbcUrlTemplate, username, password, defaultSchema);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.abhipsa.digital.law.entity");
        emf.setPersistenceUnitName("default");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.session_factory.interceptor", auditInterceptor);
        // Building the EMF by hand bypasses Spring Boot's HibernateJpaConfiguration,
        // which is what normally applies these two naming strategy defaults - without
        // them Hibernate falls back to its own JPA-compliant defaults (identifiers used
        // as-is, e.g. table "CaseDetails" instead of "case_details"), silently missing
        // the real, pre-existing snake_case schema and auto-creating empty new tables.
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl");
        properties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.hibernate.SpringImplicitNamingStrategy");
        emf.setJpaPropertyMap(properties);
        return emf;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = entityManagerFactory(dataSource);
        emf.afterPropertiesSet();
        return new JpaTransactionManager(emf.getObject());
    }
}
