package com.abhipsa.digital.law.config;

import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.UserRepository;
import com.abhipsa.digital.law.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// Validates the Bearer token (signature + expiration) on every request and,
// if valid, marks it authenticated for Spring Security. A missing, invalid,
// or expired token simply leaves the request unauthenticated; SecurityConfig
// then rejects it with 401 for any endpoint other than /auth/**. This is what
// makes the JWT's exp claim (jwt.expiration, default 24h) actually enforce a
// daily session expiry instead of being a value nobody checks.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtService.extractEmail(token);
                User user = email != null ? userRepository.findByEmail(email).orElse(null) : null;

                if (user != null) {
                    String role = user.getRole() != null ? user.getRole().toUpperCase(Locale.ROOT) : "ASSOCIATE";
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
