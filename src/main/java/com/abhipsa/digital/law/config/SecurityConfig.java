package com.abhipsa.digital.law.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // Comma-separated list, set via CORS_ALLOWED_ORIGINS. Covers local dev
    // (Vite) and a local minikube port-forward by default; add the real
    // cluster's domain here (env var, no code change) before that deployment.
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:8081}")
    private String allowedOriginsProperty;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        // Kubernetes/Docker liveness & readiness probes.
                        .requestMatchers("/actuator/health/**").permitAll()
                        // Finance and the Admin-only tools (Checker approval,
                        // Mobile Contacts). Associates/senior associates only
                        // get "main work" + change password, per FR request.
                        .requestMatchers("/api/bills/**").hasRole("ADMIN")
                        .requestMatchers("/api/mobile-contacts/**").hasRole("ADMIN")
                        .requestMatchers("/api/cases/checker/**").hasRole("ADMIN")
                        // Audit trail — who changed what, admin-only.
                        .requestMatchers("/api/audit/**").hasRole("ADMIN")
                        // Admin or senior associate can create new personnel
                        // accounts (senior associate's role is then forced
                        // server-side to "associate" — see UserService.create()).
                        // GET (dropdowns, self-lookup) stays open to everyone.
                        .requestMatchers(HttpMethod.POST, "/api/users").hasAnyRole("ADMIN", "SENIOR_ASSOCIATE")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // Missing/expired/invalid token: the frontend specifically
                        // watches for 401 to clear the stale token and redirect to
                        // login (Spring's default entry point returns 403 instead).
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"Session expired or not authenticated. Please log in again.\"}");
                        })
                        // Valid session, but the role doesn't allow this endpoint.
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"You do not have permission to access this.\"}");
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // 2. Define the exact CORS policy rule settings
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origin allowlist comes from CORS_ALLOWED_ORIGINS (see field above) —
        // works the same in local dev, a minikube port-forward, Docker
        // Compose, or a real cluster; only the env var changes, never this code.
        List<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOriginPatterns(allowedOrigins);

        // Allow typical standard REST Methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow cookies/auth tokens to slide across origins safely if needed
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply globally to all backend endpoints
        return source;
    }
}