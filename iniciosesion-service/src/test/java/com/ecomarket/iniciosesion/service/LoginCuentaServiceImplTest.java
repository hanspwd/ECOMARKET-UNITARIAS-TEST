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
 */
@ExtendWith(MockitoExtension.class)
class LoginCuentaServiceImplTest {

    // ── BCryptPasswordEncoder de referencia para verificar hashes en asserts ──
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
        @DisplayName("asigna ROLE_USER por defecto si el rol en la BD es null (cubre línea 81)")
        void iniciarSesionRolNullAsignaPorDefecto() {
            Credencial cred = credencialActiva(20L, "sinrol@eco.cl", "pass123");
            cred.setRolAcceso(null);

            when(credencialRepository.findByCorreoAcceso("sinrol@eco.cl")).thenReturn(Optional.of(cred));
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(jwtUtil.generarToken(any(), any(), any())).thenReturn("token-generado");
            when(jwtUtil.getExpirationMs()).thenReturn(3600000L);

            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("sinrol@eco.cl");
            req.setContrasena("pass123");

            IniciarSesionResponse res = service.iniciarSesion(req);

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

    // ── ═══════════════════════════════════════════════════════════════════════
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

            // AQUÍ ESTABA EL PROBLEMA: Cambia la línea original por estas dos de abajo 👇
            Claims claimsFalsos = mockClaims();
            when(jwtUtil.validarYObtenerClaims(token)).thenReturn(claimsFalsos);

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
        @DisplayName("cierra la sesión exitosamente y guarda en la blacklist (cubre líneas 112-127)")
        void cerrarSesionExitosoGuardaEnBD() {
            String token = "token-valido";
            when(jwtUtil.esTokenValido(token)).thenReturn(true);
            when(sesionJWTRepository.existsByToken(token)).thenReturn(false);

            Claims claims = mock(Claims.class);
            when(claims.getIssuedAt()).thenReturn(new Date());
            when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000L));

            when(jwtUtil.validarYObtenerClaims(token)).thenReturn(claims);
            when(jwtUtil.obtenerUsuarioId(token)).thenReturn(1L);
            when(jwtUtil.obtenerRoles(token)).thenReturn(List.of("ROLE_ADMIN"));
            when(sesionJWTRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken(token);

            MensajeResponse res = service.cerrarSesion(req);

            assertThat(res.getMensaje()).contains("exitosa");
            verify(sesionJWTRepository).save(any());
        }

        @Test
        @DisplayName("cierra la sesión asignando ROLE_USER si la lista de roles existe pero está vacía (cubre la otra mitad de la línea 114)")
        void cerrarSesionListaRolesVaciaAsignaPorDefecto() {
            String token = "token-roles-vacios";
            when(jwtUtil.esTokenValido(token)).thenReturn(true);
            when(sesionJWTRepository.existsByToken(token)).thenReturn(false);

            Claims claims = mock(Claims.class);
            when(claims.getIssuedAt()).thenReturn(new Date());
            when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000L));

            when(jwtUtil.validarYObtenerClaims(token)).thenReturn(claims);
            when(jwtUtil.obtenerUsuarioId(token)).thenReturn(1L);

            // ¡AQUÍ ESTÁ LA MAGIA! Pasamos una lista vacía en lugar de null
            when(jwtUtil.obtenerRoles(token)).thenReturn(List.of());

            when(sesionJWTRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken(token);

            service.cerrarSesion(req);

            // Verificamos que se haya guardado con ROLE_USER por defecto
            verify(sesionJWTRepository).save(argThat(s -> "ROLE_USER".equals(s.getRolAcceso())));
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
            CambiarContrasenaRequest req = new CambiarContrasenaRequest();
            req.setUsuarioId(99L);
            req.setContrasenaActual("vieja123");
            req.setNuevaContrasena("nueva456");

            when(credencialRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cambiarContrasena(req))
                    .isInstanceOf(CredencialNotFoundException.class)
                    .hasMessageContaining("No se encontraron credenciales para el usuario 99");

            verify(credencialRepository, never()).save(any());
        }

        @Test
        @DisplayName("cambia la contraseña exitosamente con datos correctos")
        void cambiaContrasenaExitosamente() {
            Credencial cred = credencialActiva(7L, "u@eco.cl", "vieja123");
            when(credencialRepository.findByUsuarioId(7L)).thenReturn(Optional.of(cred));
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CambiarContrasenaRequest req = new CambiarContrasenaRequest();
            req.setUsuarioId(7L);
            req.setContrasenaActual("vieja123");
            req.setNuevaContrasena("nueva456");

            MensajeResponse res = service.cambiarContrasena(req);

            assertThat(res.getMensaje()).contains("Contraseña actualizada");
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

    // ═════════════════════════════════════════════════════════════════════════
    // cambiarCorreo
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("cambiarCorreo")
    class CambiarCorreo {

        @Test
        @DisplayName("cambia el correo exitosamente si la contraseña es correcta y el nuevo correo está disponible")
        void cambiaCorreoExitosamente() {
            Credencial cred = credencialActiva(1L, "antiguo@eco.cl", "pass123");

            when(credencialRepository.findByUsuarioId(1L)).thenReturn(Optional.of(cred));
            when(credencialRepository.existsByCorreoAcceso("nuevo@eco.cl")).thenReturn(false);
            when(credencialRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CambiarCorreoRequest req = new CambiarCorreoRequest();
            req.setUsuarioId(1L);
            req.setContrasenaActual("pass123");
            req.setNuevoCorreo("nuevo@eco.cl");

            MensajeResponse res = service.cambiarCorreo(req);

            assertThat(res.getMensaje()).contains("Correo actualizado exitosamente");
            assertThat(cred.getCorreoAcceso()).isEqualTo("nuevo@eco.cl");
            verify(credencialRepository, times(1)).save(cred);
        }

        @Test
        @DisplayName("lanza CredencialNotFoundException si el usuario no existe en la BD")
        void usuarioInexistenteLanzaExcepcion() {
            when(credencialRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

            CambiarCorreoRequest req = new CambiarCorreoRequest();
            req.setUsuarioId(99L);

            assertThatThrownBy(() -> service.cambiarCorreo(req))
                    .isInstanceOf(CredencialNotFoundException.class)
                    .hasMessageContaining("No se encontraron credenciales para el usuario 99");

            verify(credencialRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza AutenticacionException si la contraseña de validación actual es incorrecta")
        void contrasenaIncorrectaLanzaExcepcion() {
            Credencial cred = credencialActiva(1L, "antiguo@eco.cl", "clave_correcta");
            when(credencialRepository.findByUsuarioId(1L)).thenReturn(Optional.of(cred));

            CambiarCorreoRequest req = new CambiarCorreoRequest();
            req.setUsuarioId(1L);
            req.setContrasenaActual("clave_erronea");
            req.setNuevoCorreo("nuevo@eco.cl");

            assertThatThrownBy(() -> service.cambiarCorreo(req))
                    .isInstanceOf(AutenticacionException.class)
                    .hasMessage("Contraseña incorrecta.");

            verify(credencialRepository, never()).existsByCorreoAcceso(any());
            verify(credencialRepository, never()).save(any());
        }

        @Test
        @DisplayName("lanza CorreoDuplicadoException si el nuevo correo ya le pertenece a otro usuario")
        void correoDuplicadoLanzaExcepcion() {
            Credencial cred = credencialActiva(1L, "antiguo@eco.cl", "pass123");
            when(credencialRepository.findByUsuarioId(1L)).thenReturn(Optional.of(cred));
            when(credencialRepository.existsByCorreoAcceso("duplicado@eco.cl")).thenReturn(true);

            CambiarCorreoRequest req = new CambiarCorreoRequest();
            req.setUsuarioId(1L);
            req.setContrasenaActual("pass123");
            req.setNuevoCorreo("duplicado@eco.cl");

            assertThatThrownBy(() -> service.cambiarCorreo(req))
                    .isInstanceOf(CorreoDuplicadoException.class)
                    .hasMessageContaining("ya está en uso");

            verify(credencialRepository, never()).save(any());
        }
    }
}