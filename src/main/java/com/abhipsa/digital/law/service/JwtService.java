package com.abhipsa.digital.law.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Role is embedded so the frontend can make UI decisions (hide
    // Finance/Admin nav for non-admins) from the token alone; the backend
    // never trusts this claim for authorization — JwtAuthFilter always
    // re-derives the role from the User row in the database.
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiration))
                .signWith(
                        SignatureAlgorithm.HS256,
                        secret)
                .compact();
    }

    // Same key derivation the deprecated signWith(alg, base64String) call
    // above uses internally (base64-decode, then HMAC key), so tokens signed
    // by generateToken() verify correctly here.
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Session/token lifetime is controlled entirely by jwt.expiration (default
    // 24h): a token signed at login stops validating once its exp has passed,
    // which is what makes a "session" expire once a day.
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}