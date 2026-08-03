package com.abhipsa.digital.law.config;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final TenantResolutionFilter tenantResolutionFilter;

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
                        // Tenant branding (firm name/logo) for the pre-login
                        // landing/login pages - resolved from the Host header
                        // by TenantResolutionFilter, no auth required.
                        .requestMatchers("/api/public/**").permitAll()
                        // Finance and the Admin-only tools (Checker approval,
                        // Mobile Contacts). Associates/senior associates only
                        // get "main work" + change password, per FR request.
                        .requestMatchers("/api/bills/**").hasRole("ADMIN")
                        .requestMatchers("/api/mobile-contacts/**").hasRole("ADMIN")
                        .requestMatchers("/api/cases/checker/**").hasRole("ADMIN")
                        // Audit trail — who changed what, admin-only.
                        .requestMatchers("/api/audit/**").hasRole("ADMIN")
                        // Anyone logged in can view branding (sidebar needs
                        // it); only admins can change it.
                        .requestMatchers(HttpMethod.PUT, "/api/branding").hasRole("ADMIN")
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
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Must resolve which tenant schema a request belongs to
                // before JwtAuthFilter runs - it looks the user up by email,
                // and that lookup has to hit the right tenant's users table.
                .addFilterBefore(tenantResolutionFilter, JwtAuthFilter.class);

        return http.build();
    }


    // CORS: allow any origin, reflected dynamically per-request rather than
    // matched against a fixed allowlist. This app authenticates with a
    // bearer JWT attached manually by JS (see axios.jsx) - never a cookie -
    // so there's no ambient-credential/session-riding risk an origin
    // allowlist would normally exist to prevent; the token itself is the
    // real security boundary, checked per-request by JwtAuthFilter.
    //
    // An allowlist here was a maintenance trap in practice: every new way of
    // reaching the app - a minikube port-forward on a random local port, a
    // NodePort, a real domain, a new tenant subdomain - needed its origin
    // added in advance or every request failed closed with a genuine 403
    // from Spring Security's own CorsFilter (confirmed: it rejects actual
    // requests outright when the Origin header doesn't match, not just
    // preflight - this isn't only a "browser blocks reading the response"
    // situation). setAllowedOriginPatterns(List.of("*")) is the one setting
    // that stays valid with allowCredentials(true) (unlike a literal
    // Access-Control-Allow-Origin: *) precisely so this doesn't need
    // revisiting for every future deployment target.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}