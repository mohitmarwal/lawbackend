package com.abhipsa.digital.law.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll());

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


//package com.abhipsa.digital.law.config;
//
//import org.springframework.context.annotation.bean;
//import org.springframework.context.annotation.configuration;
//import org.springframework.security.config.annotation.web.builders.httpsecurity;
//import org.springframework.security.config.http.sessioncreationpolicy;
//import org.springframework.security.web.securityfilterchain;
//
//@configuration
//public class securityconfig {
//
//    @bean
//    public securityfilterchain securityfilterchain(httpsecurity http) throws exception {
//        http
//                .csrf(csrf -> csrf.disable())
//
//                .authorizehttprequests(auth -> auth
//                        // change from "/api/auth/**" to "/auth/**" to match your actual url
//                        .requestmatchers("/auth/**").permitall()
//
//                        // keep your other role protections intact
//                        .requestmatchers("/users/**").hasrole("admin")
//                        .requestmatchers("/master-data/**").hasrole("admin")
//                        .requestmatchers("/bills/**").hasanyrole("admin", "lawyer")
//                        .requestmatchers("/cases/**", "/tasks/**").hasanyrole("admin", "lawyer", "associate")
//
//                        .anyrequest().authenticated()
//                )
//
//                .sessionmanagement(session -> session
//                        .sessioncreationpolicy(sessioncreationpolicy.stateless)
//                );
//
//        return http.build();
//    }
//}