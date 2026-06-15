package com.ecomarket.iniciosesion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas unitarias para JwtUtil.
 * NO usa @SpringBootTest — instancia el bean manualmente con ReflectionTestUtils
 * para inyectar los @Value sin levantar el contexto de Spring ni MySQL.
 *
 * Ejecutar:
 *   mvn test -pl iniciosesion-service -Dtest=JwtUtilTest
 */
class JwtUtilTest {

    // Secret de al menos 256 bits (32 chars) para HS256
    private static final String SECRET =
            "7b9b1d2e4f6a8c0e2d4f6a8b0c2e4f6a8b0c2e4f6a8b0c2e4f6a8b0c2e4f6a8b";
    private static final long EXPIRATION_MS = 86_400_000L; // 24h

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        // Instanciamos JwtUtil directamente pasando los mismos valores que application.properties
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_MS);
    }

    // ── Helper: genera un token válido de prueba ──────────────────────────────
    private String tokenValido() {
        return jwtUtil.generarToken(42L, "hocx@eco.cl", List.of("ROLE_USER"));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // generarToken
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("generarToken")
    class GenerarToken {

        @Test
        @DisplayName("retorna un string JWT no nulo con formato ey...")
        void retornaTokenNoNulo() {
            String token = jwtUtil.generarToken(1L, "test@eco.cl", List.of("ROLE_USER"));
            assertThat(token).isNotNull().startsWith("ey");
        }

        @Test
        @DisplayName("el token generado contiene tres partes separadas por punto")
        void tokenTieneFormatoJWT() {
            String token = jwtUtil.generarToken(1L, "test@eco.cl", List.of("ROLE_USER"));
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("tokens distintos para distintos usuarios")
        void tokensDiferentesParaUsuariosDiferentes() {
            String t1 = jwtUtil.generarToken(1L, "a@eco.cl", List.of("ROLE_USER"));
            String t2 = jwtUtil.generarToken(2L, "b@eco.cl", List.of("ROLE_USER"));
            assertThat(t1).isNotEqualTo(t2);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // esTokenValido
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("esTokenValido")
    class EsTokenValido {

        @Test
        @DisplayName("retorna true para un token recién generado")
        void tokenRecienGeneradoEsValido() {
            assertThat(jwtUtil.esTokenValido(tokenValido())).isTrue();
        }

        @Test
        @DisplayName("retorna false para un string aleatorio")
        void stringAleatorioEsInvalido() {
            assertThat(jwtUtil.esTokenValido("esto.no.es.jwt")).isFalse();
        }

        @Test
        @DisplayName("retorna false para un token vacío")
        void tokenVacioEsInvalido() {
            assertThat(jwtUtil.esTokenValido("")).isFalse();
        }

        @Test
        @DisplayName("retorna false para un token firmado con otra clave")
        void tokenFirmadoConOtraClaveEsInvalido() {
            // Creamos un segundo JwtUtil con clave diferente
            JwtUtil otroUtil = new JwtUtil(
                    "aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899",
                    EXPIRATION_MS);
            String tokenAjeno = otroUtil.generarToken(1L, "x@eco.cl", List.of("ROLE_USER"));
            assertThat(jwtUtil.esTokenValido(tokenAjeno)).isFalse();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // obtenerCorreo / obtenerUsuarioId / obtenerRoles
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("extracción de claims")
    class ExtraccionClaims {

        @Test
        @DisplayName("obtenerCorreo retorna el subject correcto")
        void obtenerCorreoCorrecto() {
            String token = jwtUtil.generarToken(42L, "hocx@eco.cl", List.of("ROLE_USER"));
            assertThat(jwtUtil.obtenerCorreo(token)).isEqualTo("hocx@eco.cl");
        }

        @Test
        @DisplayName("obtenerUsuarioId retorna el id correcto")
        void obtenerUsuarioIdCorrecto() {
            String token = jwtUtil.generarToken(99L, "u@eco.cl", List.of("ROLE_ADMIN"));
            assertThat(jwtUtil.obtenerUsuarioId(token)).isEqualTo(99L);
        }

        @Test
        @DisplayName("obtenerRoles retorna la lista de roles correcta")
        void obtenerRolesCorrecto() {
            String token = jwtUtil.generarToken(1L, "u@eco.cl", List.of("ROLE_ADMIN", "ROLE_USER"));
            assertThat(jwtUtil.obtenerRoles(token)).containsExactly("ROLE_ADMIN", "ROLE_USER");
        }

        @Test
        @DisplayName("obtenerExpiracion retorna fecha futura")
        void obtenerExpiracionEsFutura() {
            String token = tokenValido();
            assertThat(jwtUtil.obtenerExpiracion(token).getTime())
                    .isGreaterThan(System.currentTimeMillis());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // getExpirationMs
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getExpirationMs retorna el valor configurado")
    void getExpirationMsRetornaValorConfigurado() {
        assertThat(jwtUtil.getExpirationMs()).isEqualTo(EXPIRATION_MS);
    }
}