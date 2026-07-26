package com.abhipsa.digital.law.config;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Pushes the Spring-managed DataSource into AuditInterceptor's static field.
 * Hibernate constructs AuditInterceptor itself (via the class-name property
 * in application.properties), so it never goes through Spring's DI — this
 * is the bridge between the two.
 */
@Component
public class AuditInterceptorWiring {

    public AuditInterceptorWiring(DataSource dataSource) {
        AuditInterceptor.setDataSource(dataSource);
    }
}
