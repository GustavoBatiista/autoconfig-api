package com.gustavobatista.autoconfig.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gustavobatista.autoconfig.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    public static final String ROLE_CLAIM = "role";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationMs;

    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Gera token com subject = email e claim opcional do papel (ROLE_ADMIN, ROLE_SELLER, …). */
    public String generateToken(String email, Role role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(exp);

        if (role != null) {
            builder.claim(ROLE_CLAIM, role.name());
        }

        return builder
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    /** Sobrecarga se no login você só tiver o email (o papel vem do banco no UserDetails). */
    public String generateToken(String email) {
        return generateToken(email, null);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Role extractRole(String token) {
        Claims claims = extractAllClaims(token);
        String raw = claims.get(ROLE_CLAIM, String.class);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean isTokenValid(String token, String expectedEmail) {
        if (token == null || expectedEmail == null) {
            return false;
        }
        try {
            String subject = extractUsername(token);
            return expectedEmail.equals(subject) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}