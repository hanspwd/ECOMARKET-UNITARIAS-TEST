package com.ecomarket.iniciosesion.service;

import java.security.Key;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class JwtUtilTest {
    private static final long expirationMs = 3600000L;
    private static final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Test
    public void generarToken_deberiaCrearTokenValido() {
        String token = generarToken(1L, "test@example.com", List.of("ROLE_USER"));
        assertNotNull(token);
    }

    private String generarToken(Long usuarioId, String correo, List<String> roles) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(correo)
                .claim("usuarioId", usuarioId)
                .claim("roles", roles)
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(secretKey)
                .compact();
    }
}
