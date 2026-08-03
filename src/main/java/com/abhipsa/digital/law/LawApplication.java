package com.abhipsa.digital.law;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceInitializationAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// The primary (tenant-routed, @Primary DataSource) JPA setup is restricted
// to just the "entity"/"repository" packages - the separate "registry"
// package (the always-on tenant lookup table, see RegistryDataSourceConfig)
// is deliberately excluded here and wired to its own independent
// DataSource/EntityManagerFactory instead, since it must never be
// tenant-routed.
//
// DataSourceInitializationAutoConfiguration is excluded: this app has never
// used schema.sql/data.sql (DDL is Hibernate ddl-auto=update), and with two
// EntityManagerFactories present its implicit @DependsOn wiring forms a
// genuine bean-creation cycle (the primary DataSource needs the registry's
// EntityManagerFactory to look up tenants, and Boot wires the initializer
// bean between the two) - safe to drop entirely since it does nothing here.
@SpringBootApplication(exclude = DataSourceInitializationAutoConfiguration.class)
@EntityScan(basePackages = "com.abhipsa.digital.law.entity")
@EnableJpaRepositories(basePackages = "com.abhipsa.digital.law.repository")
public class LawApplication {

	public static void main(String[] args) {
		SpringApplication.run(LawApplication.class, args);
	}

}
