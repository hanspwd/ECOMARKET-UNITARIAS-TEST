package com.ecomarket.iniciosesion.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // ── Generar token ─────────────────────────────────────────────────────────

    public String generarToken(Long usuarioId, String correo, List<String> roles) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(correo)
                .claim("usuarioId", usuarioId)
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    // ── Validar y extraer claims ──────────────────────────────────────────────

    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esTokenValido(String token) {
        try {
            validarYObtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String obtenerCorreo(String token) {
        return validarYObtenerClaims(token).getSubject();
    }

    public Long obtenerUsuarioId(String token) {
        Claims claims = validarYObtenerClaims(token);
        return claims.get("usuarioId", Long.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> obtenerRoles(String token) {
        Claims claims = validarYObtenerClaims(token);
        return (List<String>) claims.get("roles");
    }

    public Date obtenerExpiracion(String token) {
        return validarYObtenerClaims(token).getExpiration();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
