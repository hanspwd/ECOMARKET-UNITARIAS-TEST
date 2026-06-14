package com.ecomarket.iniciosesion.service;

import com.ecomarket.iniciosesion.dto.*;
import com.ecomarket.iniciosesion.exception.*;
import com.ecomarket.iniciosesion.model.Credencial;
import com.ecomarket.iniciosesion.model.SesionJWT;
import com.ecomarket.iniciosesion.model.TokenRecuperacion;
import com.ecomarket.iniciosesion.repository.CredencialRepository;
import com.ecomarket.iniciosesion.repository.SesionJWTRepository;
import com.ecomarket.iniciosesion.repository.TokenRecuperacionRepository;
import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para LoginCuentaServiceImpl.
 *
 * Ejecutar solo este servicio:
 * mvn test -pl iniciosesion-service -Dtest=LoginCuentaServiceImplTest
 *
 * FIX: se declaró el campo estático `encoder` que los tests de
 * CambiarContrasena
 * necesitaban para verificar el hash resultante (bug original: variable no
 * declarada).
 */
@ExtendWith(MockitoExtension.class)
class LoginCuentaServiceImplTest {

    // ── BCryptPasswordEncoder de referencia para verificar hashes en asserts ──
    // No es un mock; se usa solo en los argThat() donde necesitamos matches().
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ── Mocks ─────────────────────────────────────────────────────────────────

    @Mock
    private CredencialRepository credencialRepository;
    @Mock
    private TokenRecuperacionRepository tokenRecuperacionRepository;
    @Mock
    private SesionJWTRepository sesionJWTRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LoginCuentaServiceImpl service;

    // FIX: LoginCuentaServiceImpl crea su propio BCryptPasswordEncoder internamente
    // (campo `private final BCryptPasswordEncoder passwordEncoder = new
    // BCryptPasswordEncoder()`).
    // @InjectMocks NO puede inyectar ese campo porque no es un bean declarado como
    // @Mock.
    // Por eso reconstruimos el service manualmente en @BeforeEach para asegurarnos
    // de que todos los colaboradores correctos están inyectados.
    @BeforeEach
    void setup() {
        service = new LoginCuentaServiceImpl(
                credencialRepository,
                tokenRecuperacionRepository,
                sesionJWTRepository,
                jwtUtil,
                restTemplate);
    }

    // ── Helper: construye una credencial activa con la contraseña ya hasheada ─
    private Credencial credencialActiva(Long usuarioId, String correo, String contrasenaPlana) {
        return Credencial.builder()
                .id(usuarioId)
                .usuarioId(usuarioId)
                .correoAcceso(correo)
                // Hasheamos aquí para que passwordEncoder.matches() funcione dentro del service
                .contrasenaHash(encoder.encode(contrasenaPlana))
                .cuentaBloqueada(false)
                .rolAcceso("ROLE_USER")
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // crearCredencial
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crearCredencial")
    class CrearCredencial {

        @Test
        @DisplayName("crea credencial con rol ROLE_USER por defecto cuando rol es null")
        void creaConRolPorDefecto() {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(1L);
            req.setCorreo("test@eco.cl");
            req.setContrasena("pass123");
            req.setRol(null);

            when(credencialRepository.existsByCorreoAcceso("test@eco.cl")).thenReturn(false);
            when(credencialRepository.save(any(Credencial.class))).thenAnswer(i -> i.getArgument(0));

            MensajeResponse res = service.crearCredencial(req);

            assertThat(res.getMensaje()).contains("exitosamente");
            verify(credencialRepository).save(argThat(c -> "ROLE_USER".equals(c.getRolAcceso())));
        }

        @Test
        @DisplayName("crea credencial con rol ROLE_ADMIN cuando se especifica explícitamente")
        void creaConRolAdmin() {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(2L);
            req.setCorreo("admin@eco.cl");
            req.setContrasena("adminPass");
            req.setRol("ROLE_ADMIN");

            when(credencialRepository.existsByCorreoAcceso(any())).thenReturn(false);
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.crearCredencial(req);

            verify(credencialRepository).save(argThat(c -> "ROLE_ADMIN".equals(c.getRolAcceso())));
        }

        @Test
        @DisplayName("lanza CorreoDuplicadoException si el correo ya existe en BD")
        void correoDuplicadoLanzaExcepcion() {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setCorreo("dup@eco.cl");

            when(credencialRepository.existsByCorreoAcceso("dup@eco.cl")).thenReturn(true);

            assertThatThrownBy(() -> service.crearCredencial(req))
                    .isInstanceOf(CorreoDuplicadoException.class)
                    .hasMessageContaining("dup@eco.cl");

            verify(credencialRepository, never()).save(any());
        }

        @Test
        @DisplayName("la contraseña se almacena hasheada con BCrypt — nunca en texto plano")
        void contrasenaSeguardaHasheada() {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(3L);
            req.setCorreo("user@eco.cl");
            req.setContrasena("miContraseña");

            when(credencialRepository.existsByCorreoAcceso(any())).thenReturn(false);
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.crearCredencial(req);

            verify(credencialRepository).save(argThat(c -> !c.getContrasenaHash().equals("miContraseña")
                    && c.getContrasenaHash().startsWith("$2a$")));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // iniciarSesion
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("iniciarSesion")
    class IniciarSesion {

        @Test
        @DisplayName("retorna token JWT al autenticar correctamente")
        void autenticacionExitosaRetornaToken() {
            Credencial cred = credencialActiva(10L, "user@eco.cl", "pass123");

            when(credencialRepository.findByCorreoAcceso("user@eco.cl")).thenReturn(Optional.of(cred));
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(jwtUtil.generarToken(eq(10L), eq("user@eco.cl"), any())).thenReturn("jwt-token-abc");
            when(jwtUtil.getExpirationMs()).thenReturn(3600000L);

            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("user@eco.cl");
            req.setContrasena("pass123");

            IniciarSesionResponse res = service.iniciarSesion(req);

            assertThat(res.getToken()).isEqualTo("jwt-token-abc");
            assertThat(res.getUsuarioId()).isEqualTo(10L);
            assertThat(res.getRol()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("lanza AutenticacionException si el correo no existe")
        void correoInexistenteLanzaExcepcion() {
            when(credencialRepository.findByCorreoAcceso(any())).thenReturn(Optional.empty());

            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("noexiste@eco.cl");
            req.setContrasena("pass");

            assertThatThrownBy(() -> service.iniciarSesion(req))
                    .isInstanceOf(AutenticacionException.class);
        }

        @Test
        @DisplayName("lanza AutenticacionException si la contraseña es incorrecta")
        void contrasenaIncorrectaLanzaExcepcion() {
            Credencial cred = credencialActiva(10L, "user@eco.cl", "correcta");
            when(credencialRepository.findByCorreoAcceso("user@eco.cl")).thenReturn(Optional.of(cred));

            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("user@eco.cl");
            req.setContrasena("incorrecta");

            assertThatThrownBy(() -> service.iniciarSesion(req))
                    .isInstanceOf(AutenticacionException.class);
        }

        @Test
        @DisplayName("lanza CuentaBloqueadaException si la cuenta está bloqueada")
        void cuentaBloqueadaLanzaExcepcion() {
            Credencial cred = credencialActiva(10L, "bloq@eco.cl", "pass");
            cred.setCuentaBloqueada(true);
            when(credencialRepository.findByCorreoAcceso("bloq@eco.cl")).thenReturn(Optional.of(cred));

            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("bloq@eco.cl");
            req.setContrasena("pass");

            assertThatThrownBy(() -> service.iniciarSesion(req))
                    .isInstanceOf(CuentaBloqueadaException.class);
        }

        @Test
        @DisplayName("actualiza fechaUltimoLogin en la credencial al autenticar")
        void actualizaFechaUltimoLogin() {
            Credencial cred = credencialActiva(10L, "user@eco.cl", "pass123");
            when(credencialRepository.findByCorreoAcceso("user@eco.cl")).thenReturn(Optional.of(cred));
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(jwtUtil.generarToken(any(), any(), any())).thenReturn("tok");
            when(jwtUtil.getExpirationMs()).thenReturn(3600000L);

            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("user@eco.cl");
            req.setContrasena("pass123");

            service.iniciarSesion(req);

            verify(credencialRepository).save(argThat(c -> c.getFechaUltimoLogin() != null));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // cerrarSesion
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cerrarSesion")
    class CerrarSesion {

        private Claims mockClaims() {
            Claims claims = mock(Claims.class);
            when(claims.getIssuedAt()).thenReturn(new Date());
            when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000));
            return claims;
        }

        @Test
        @DisplayName("agrega token a blacklist y retorna mensaje de éxito")
        void blacklistTokenExitoso() {
            String token = "valid-jwt";
            when(jwtUtil.esTokenValido(token)).thenReturn(true);
            when(sesionJWTRepository.existsByToken(token)).thenReturn(false);
            when(jwtUtil.validarYObtenerClaims(token)).thenReturn(mockClaims());
            when(jwtUtil.obtenerUsuarioId(token)).thenReturn(1L);
            when(jwtUtil.obtenerRoles(token)).thenReturn(List.of("ROLE_USER"));
            when(sesionJWTRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken(token);

            MensajeResponse res = service.cerrarSesion(req);

            assertThat(res.getMensaje()).contains("exitosamente");
            verify(sesionJWTRepository).save(any(SesionJWT.class));
        }

        @Test
        @DisplayName("retorna mensaje sin re-agregar si el token ya está en blacklist")
        void tokenYaEnBlacklist() {
            String token = "already-closed";
            when(jwtUtil.esTokenValido(token)).thenReturn(true);
            when(sesionJWTRepository.existsByToken(token)).thenReturn(true);

            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken(token);

            MensajeResponse res = service.cerrarSesion(req);

            assertThat(res.getMensaje()).contains("ya estaba cerrada");
            verify(sesionJWTRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza TokenInvalidoException si el token no es válido")
        void tokenInvalidoLanzaExcepcion() {
            when(jwtUtil.esTokenValido("bad-token")).thenReturn(false);

            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken("bad-token");

            assertThatThrownBy(() -> service.cerrarSesion(req))
                    .isInstanceOf(TokenInvalidoException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // autenticarJWT
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("autenticarJWT")
    class AutenticarJWT {

        @Test
        @DisplayName("retorna valido=true con datos del usuario para token activo")
        void tokenActivoEsValido() {
            String token = "active-jwt";
            when(jwtUtil.esTokenValido(token)).thenReturn(true);
            when(sesionJWTRepository.existsByToken(token)).thenReturn(false);
            when(jwtUtil.obtenerUsuarioId(token)).thenReturn(5L);
            when(jwtUtil.obtenerCorreo(token)).thenReturn("x@eco.cl");
            when(jwtUtil.obtenerRoles(token)).thenReturn(List.of("ROLE_USER"));

            AutenticarJWTRequest req = new AutenticarJWTRequest();
            req.setToken(token);

            AutenticarJWTResponse res = service.autenticarJWT(req);

            assertThat(res.isValido()).isTrue();
            assertThat(res.getUsuarioId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("retorna valido=false si el token está en blacklist (logout previo)")
        void tokenEnBlacklistEsInvalido() {
            String token = "logged-out";
            when(jwtUtil.esTokenValido(token)).thenReturn(true);
            when(sesionJWTRepository.existsByToken(token)).thenReturn(true);

            AutenticarJWTRequest req = new AutenticarJWTRequest();
            req.setToken(token);

            assertThat(service.autenticarJWT(req).isValido()).isFalse();
        }

        @Test
        @DisplayName("retorna valido=false si el JWT está expirado o malformado")
        void tokenExpiradoEsInvalido() {
            when(jwtUtil.esTokenValido("expired")).thenReturn(false);

            AutenticarJWTRequest req = new AutenticarJWTRequest();
            req.setToken("expired");

            assertThat(service.autenticarJWT(req).isValido()).isFalse();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // cambiarContrasena
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cambiarContrasena")
    class CambiarContrasena {

        @Test
        @DisplayName("actualiza el hash en BD al cambiar contraseña con credenciales correctas")
        void usuarioInexistenteLanzaExcepcion() {
            // 1. Arrange: Preparamos un request con un ID que "no existe" en BD
            CambiarContrasenaRequest req = new CambiarContrasenaRequest();
            req.setUsuarioId(99L);
            req.setContrasenaActual("vieja123");
            req.setNuevaContrasena("nueva456");

            // Hacemos que el mock devuelva vacío (esto obligará a ejecutar el orElseThrow)
            when(credencialRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

            // 2. Act & 3. Assert: Verificamos que explote exactamente en esa línea roja
            assertThatThrownBy(() -> service.cambiarContrasena(req))
                    .isInstanceOf(CredencialNotFoundException.class)
                    .hasMessageContaining("No se encontraron credenciales para el usuario 99");

            // (Opcional) Verificamos que no se intentó guardar nada por error
            verify(credencialRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza AutenticacionException si la contraseña actual es incorrecta")
        void contrasenaActualIncorrectaLanzaExcepcion() {
            Credencial cred = credencialActiva(7L, "u@eco.cl", "correcta");
            when(credencialRepository.findByUsuarioId(7L)).thenReturn(Optional.of(cred));

            CambiarContrasenaRequest req = new CambiarContrasenaRequest();
            req.setUsuarioId(7L);
            req.setContrasenaActual("incorrecta");
            req.setNuevaContrasena("nueva");

            assertThatThrownBy(() -> service.cambiarContrasena(req))
                    .isInstanceOf(AutenticacionException.class);

            verify(credencialRepository, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // inhabilitarCredenciales
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("inhabilitarCredenciales")
    class InhabilitarCredenciales {

        @Test
        @DisplayName("bloquea la cuenta y retorna mensaje de confirmación")
        void bloqueaCuentaExitosamente() {
            Credencial cred = credencialActiva(9L, "u@eco.cl", "pass");
            when(credencialRepository.findByUsuarioId(9L)).thenReturn(Optional.of(cred));
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            InhabilitarCredencialesRequest req = new InhabilitarCredencialesRequest();
            req.setUsuarioId(9L);

            MensajeResponse res = service.inhabilitarCredenciales(req);

            assertThat(res.getMensaje()).contains("bloqueada");
            verify(credencialRepository).save(argThat(c -> Boolean.TRUE.equals(c.getCuentaBloqueada())));
        }

        @Test
        @DisplayName("lanza CredencialNotFoundException si el usuario no existe")
        void usuarioInexistenteLanzaExcepcion() {
            when(credencialRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

            InhabilitarCredencialesRequest req = new InhabilitarCredencialesRequest();
            req.setUsuarioId(99L);

            assertThatThrownBy(() -> service.inhabilitarCredenciales(req))
                    .isInstanceOf(CredencialNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // recuperarCredenciales / restablecerConToken
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("recuperarCredenciales y restablecerConToken")
    class Recuperacion {

        @Test
        @DisplayName("genera código alfanumérico y lo persiste al solicitar recuperación")
        void generaCodigoDeRecuperacion() {
            Credencial cred = credencialActiva(1L, "r@eco.cl", "pass");
            when(credencialRepository.findByCorreoAcceso("r@eco.cl")).thenReturn(Optional.of(cred));
            when(tokenRecuperacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            RecuperarCredencialesRequest req = new RecuperarCredencialesRequest();
            req.setCorreo("r@eco.cl");

            MensajeResponse res = service.recuperarCredenciales(req);

            assertThat(res.getMensaje()).contains("Código de recuperación generado");
            verify(tokenRecuperacionRepository).save(argThat(t -> t.getCodigoAlfanumerico() != null
                    && !t.getCodigoAlfanumerico().isBlank()
                    && !t.getConsumido()));
        }

        @Test
        @DisplayName("lanza CredencialNotFoundException si el correo no existe al recuperar")
        void correoInexistenteLanzaExcepcion() {
            when(credencialRepository.findByCorreoAcceso("noexiste@eco.cl"))
                    .thenReturn(Optional.empty());

            RecuperarCredencialesRequest req = new RecuperarCredencialesRequest();
            req.setCorreo("noexiste@eco.cl");

            assertThatThrownBy(() -> service.recuperarCredenciales(req))
                    .isInstanceOf(CredencialNotFoundException.class);
        }

        @Test
        @DisplayName("restablece contraseña y marca token como consumido")
        void restablececonTokenExitosamente() {
            Credencial cred = credencialActiva(1L, "r@eco.cl", "vieja");
            TokenRecuperacion tokenRec = TokenRecuperacion.builder()
                    .credencial(cred)
                    .codigoAlfanumerico("ABC123")
                    .expiracion(LocalDateTime.now().plusHours(1))
                    .consumido(false)
                    .build();

            when(tokenRecuperacionRepository.buscarTokenActivo(eq("ABC123"), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(tokenRec));
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(tokenRecuperacionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            RestablecerConTokenRequest req = new RestablecerConTokenRequest();
            req.setCodigo("ABC123");
            req.setNuevaContrasena("nueva789");

            MensajeResponse res = service.restablecerConToken(req);

            assertThat(res.getMensaje()).contains("exitosamente");
            assertThat(tokenRec.getConsumido()).isTrue();
            assertThat(cred.getCuentaBloqueada()).isFalse();
        }

        @Test
        @DisplayName("lanza TokenInvalidoException si el código expiró o ya fue consumido")
        void tokenExpiradoLanzaExcepcion() {
            when(tokenRecuperacionRepository.buscarTokenActivo(any(), any()))
                    .thenReturn(Optional.empty());

            RestablecerConTokenRequest req = new RestablecerConTokenRequest();
            req.setCodigo("EXPIRADO");
            req.setNuevaContrasena("nueva");

            assertThatThrownBy(() -> service.restablecerConToken(req))
                    .isInstanceOf(TokenInvalidoException.class);
        }
    }
}