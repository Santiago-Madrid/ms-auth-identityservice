package com.wd.ms_auth.identityservice.service;

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
    /**
     * Inyectamos la clave secreta en el service que viene del yaml
     */
    @Value("${jwt.secret}")
    String secretKey;

    /**
     * Inyectamos la clave secreta en el service que viene del yaml
     */
    @Value("${security.jwt.token-expiration}")
    Long tokenExpiration;

    /**
     * Transforma la clave secreta de String (BASE64) a un obejto SecretKey
     * utilizable por la libreria
     * 
     * @return firma secreta
     */
    private SecretKey getSignKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generar el token de seguridad al iniciar sesion
     * 
     * @param userId
     * @param email
     * @return jwt
     */
    public String generateToken(Long userId,String email) {
        return Jwts.builder()
                .claim("userId", userId)
                .subject(email) 
                .issuedAt(new Date()) 
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSignKey()) 
                .compact();
    }

    /**
     * Verifica si el token es válido
     * 
     * @param token
     * @return boleano
     */
    public Boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extraer todos los claims del token
     * 
     * @param <T>
     * @param token
     * @param resolver
     * @return claims
     */
    public <T> T extractClaims(String token, Function<Claims, T> resolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    /**
     * Extraer el nombre de usuario del token
     * 
     * @param token
     * @return nombre de usuario
     */
    public String extractEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * Extrae el id del usuario
     * 
     * @param token
     * @return id del usuario
     */
    public Long extractUserId(String token) {
        return extractClaims(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Refresca el token de seguridad, generando uno nuevo con la misma información pero con nueva expiración
     * @param token
     * @return generar nuevo token
     * @throws Exception
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
            throw new Exception("Token is expired" + e.getMessage());
        } catch (JwtException e) {
            throw new Exception("Token is invalid" + e.getMessage());
        }

        // Generamos nuevo token con nueva expiracion
        return generateToken(claims.get("userId", Long.class),
        claims.getSubject());
    }
}
