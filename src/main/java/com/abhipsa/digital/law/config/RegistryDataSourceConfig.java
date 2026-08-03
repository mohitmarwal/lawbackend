package com.abhipsa.digital.law.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// A second, completely independent DataSource/EntityManagerFactory pointed
// permanently at the "tenant_registry" schema - the one thing that must be
// reachable *before* a tenant is known (it's what resolves a subdomain to a
// tenant schema in the first place), so it can never itself be tenant-routed.
// The main (tenant-routed) DataSource stays @Primary (see
// TenantDataSourceConfig) - this class only wires up the secondary one,
// scoped to the "registry" package.
//
// Built via LocalContainerEntityManagerFactoryBean directly (not the
// autoconfigured EntityManagerFactoryBuilder helper) - see the comment on
// TenantDataSourceConfig for why: that shared builder bean itself needs a
// DataSource to construct, which created a cycle against the primary
// DataSource (which needs this registry EntityManagerFactory to look up
// tenants).
@Configuration
@EnableJpaRepositories(
        basePackages = "com.abhipsa.digital.law.registry",
        entityManagerFactoryRef = "registryEntityManagerFactory",
        transactionManagerRef = "registryTransactionManager"
)
@EntityScan(basePackages = "com.abhipsa.digital.law.registry")
public class RegistryDataSourceConfig {

    @Value("${registry.datasource.url}")
    private String url;

    @Value("${registry.datasource.username}")
    private String username;

    @Value("${registry.datasource.password}")
    private String password;

    @Bean
    public DataSource registryDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setPoolName("tenant-registry");
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean registryEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(registryDataSource());
        emf.setPackagesToScan("com.abhipsa.digital.law.registry");
        emf.setPersistenceUnitName("registry");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl");
        properties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.hibernate.SpringImplicitNamingStrategy");
        emf.setJpaPropertyMap(properties);
        return emf;
    }

    @Bean
    public PlatformTransactionManager registryTransactionManager() {
        LocalContainerEntityManagerFactoryBean emf = registryEntityManagerFactory();
        emf.afterPropertiesSet();
        return new JpaTransactionManager(emf.getObject());
    }
}
