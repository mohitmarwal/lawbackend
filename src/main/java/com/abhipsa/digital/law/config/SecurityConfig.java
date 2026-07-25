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
                        // Only an admin can create new personnel accounts;
                        // GET (dropdowns, self-lookup) stays open to everyone.
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
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

        // Explicitly allow your Vite React Local Server Origin
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

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