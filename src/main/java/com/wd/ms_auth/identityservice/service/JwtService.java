package com.wd.ms_auth.identityservice.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.token-expiration}")
    private Long tokenExpiration;

    /**
     * Convierte la clave secreta en un objeto SecretKey.
     */
    private SecretKey getSignKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera el JWT incluyendo el ID de usuario, email y el ID del rol.
     */
    public String generateToken(Long userId, String email, Long roleId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("roleId", roleId)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Sobrecarga del método por compatibilidad.
     */
    public String generateToken(Long userId, String email) {
        return generateToken(userId, email, null);
    }

    /**
     * Valida la firma y expiración del token.
     */
    public Boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extrae un claim específico mediante una función resolver.
     */
    public <T> T extractClaims(String token, Function<Claims, T> resolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    public String extractEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * Extrae el userId mapeando de forma segura de Integer/Number a Long.
     */
    public Long extractUserId(String token) {
        return extractClaims(token, claims -> {
            Object userId = claims.get("userId");
            return userId instanceof Number ? ((Number) userId).longValue() : null;
        });
    }

    /**
     * Extrae el roleId del token.
     */
    public Long extractRoleId(String token) {
        return extractClaims(token, claims -> {
            Object roleId = claims.get("roleId");
            return roleId instanceof Number ? ((Number) roleId).longValue() : null;
        });
    }

    /**
     * Genera un nuevo token basándose en los claims de un token existente.
     */
    public String refreshToken(String token) throws Exception {
        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims(); // Permite refrescar tokens recién expirados
        } catch (JwtException e) {
            throw new Exception("Token inválido: " + e.getMessage());
        }

        Object rawUserId = claims.get("userId");
        Object rawRoleId = claims.get("roleId");

        Long userId = rawUserId instanceof Number ? ((Number) rawUserId).longValue() : null;
        Long roleId = rawRoleId instanceof Number ? ((Number) rawRoleId).longValue() : null;

        return generateToken(userId, claims.getSubject(), roleId);
    }
}