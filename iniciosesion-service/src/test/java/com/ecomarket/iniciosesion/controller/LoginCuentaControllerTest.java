package com.ecomarket.iniciosesion.controller;

import com.ecomarket.iniciosesion.dto.*;
import com.ecomarket.iniciosesion.exception.*;
import com.ecomarket.iniciosesion.service.LoginCuentaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración web para LoginCuentaController.
 * Usa MockMvc para disparar HTTP sin levantar servidor real.
 * La BD se simula con H2 (perfil "test").
 *
 * Ejecutar:
 *   mvn test -pl iniciosesion-service -Dtest=LoginCuentaControllerTest
 */
@WebMvcTest(LoginCuentaController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LoginCuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LoginCuentaService loginCuentaService;

    // ── Helper: serializa objeto a JSON ───────────────────────────────────────
    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/sesion/credencial
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/sesion/credencial")
    class CrearCredencial {

        @Test
        @DisplayName("201 CREATED con mensaje de éxito al crear credencial válida")
        void crearCredencialExitoso() throws Exception {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(1L);
            req.setCorreo("hocx@eco.cl");
            req.setContrasena("pass1234");

            when(loginCuentaService.crearCredencial(any()))
                    .thenReturn(MensajeResponse.de("Credencial creada exitosamente."));

            mockMvc.perform(post("/api/sesion/credencial")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mensaje").value("Credencial creada exitosamente."));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando el correo tiene formato inválido")
        void crearCredencialCorreoInvalido() throws Exception {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(1L);
            req.setCorreo("no-es-un-correo");
            req.setContrasena("pass1234");

            mockMvc.perform(post("/api/sesion/credencial")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando la contraseña tiene menos de 8 caracteres")
        void crearCredencialContrasenaMuyCorta() throws Exception {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(1L);
            req.setCorreo("hocx@eco.cl");
            req.setContrasena("123");

            mockMvc.perform(post("/api/sesion/credencial")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("409 CONFLICT cuando el correo ya está registrado")
        void crearCredencialCorreoDuplicado() throws Exception {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(2L);
            req.setCorreo("dup@eco.cl");
            req.setContrasena("pass1234");

            when(loginCuentaService.crearCredencial(any()))
                    .thenThrow(new CorreoDuplicadoException("El correo 'dup@eco.cl' ya está registrado."));

            mockMvc.perform(post("/api/sesion/credencial")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isConflict());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/sesion/login
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/sesion/login")
    class Login {

        @Test
        @DisplayName("200 OK con token JWT en el body al autenticar correctamente")
        void loginExitoso() throws Exception {
            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("hocx@eco.cl");
            req.setContrasena("pass1234");

            IniciarSesionResponse resp = IniciarSesionResponse.builder()
                    .token("jwt-abc")
                    .usuarioId(1L)
                    .correo("hocx@eco.cl")
                    .rol("ROLE_USER")
                    .expiracionMs(86400000L)
                    .build();

            when(loginCuentaService.iniciarSesion(any())).thenReturn(resp);

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-abc"))
                    .andExpect(jsonPath("$.usuarioId").value(1))
                    .andExpect(jsonPath("$.rol").value("ROLE_USER"));
        }

        @Test
        @DisplayName("401 UNAUTHORIZED cuando las credenciales son incorrectas")
        void loginCredencialesIncorrectas() throws Exception {
            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("hocx@eco.cl");
            req.setContrasena("wrong");

            when(loginCuentaService.iniciarSesion(any()))
                    .thenThrow(new AutenticacionException("Correo o contraseña incorrectos."));

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 FORBIDDEN cuando la cuenta está bloqueada")
        void loginCuentaBloqueada() throws Exception {
            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("bloq@eco.cl");
            req.setContrasena("pass1234");

            when(loginCuentaService.iniciarSesion(any()))
                    .thenThrow(new CuentaBloqueadaException("La cuenta está bloqueada."));

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isForbidden());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/sesion/logout
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/sesion/logout")
    class Logout {

        @Test
        @DisplayName("200 OK al cerrar sesión con token válido")
        void logoutExitoso() throws Exception {
            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken("valid-jwt");

            when(loginCuentaService.cerrarSesion(any()))
                    .thenReturn(MensajeResponse.de("Sesión cerrada exitosamente."));

            mockMvc.perform(post("/api/sesion/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value("Sesión cerrada exitosamente."));
        }

        @Test
        @DisplayName("401 UNAUTHORIZED cuando el token es inválido")
        void logoutTokenInvalido() throws Exception {
            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken("bad-token");

            when(loginCuentaService.cerrarSesion(any()))
                    .thenThrow(new TokenInvalidoException("El token no es válido."));

            mockMvc.perform(post("/api/sesion/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/sesion/validar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/sesion/validar")
    class ValidarJWT {

        @Test
        @DisplayName("200 OK con valido=true para token activo")
        void validarTokenActivo() throws Exception {
            AutenticarJWTRequest req = new AutenticarJWTRequest();
            req.setToken("active-jwt");

            AutenticarJWTResponse resp = AutenticarJWTResponse.builder()
                    .valido(true)
                    .usuarioId(5L)
                    .correo("x@eco.cl")
                    .roles(List.of("ROLE_USER"))
                    .build();

            when(loginCuentaService.autenticarJWT(any())).thenReturn(resp);

            mockMvc.perform(post("/api/sesion/validar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido").value(true))
                    .andExpect(jsonPath("$.usuarioId").value(5));
        }

        @Test
        @DisplayName("200 OK con valido=false para token en blacklist")
        void validarTokenEnBlacklist() throws Exception {
            AutenticarJWTRequest req = new AutenticarJWTRequest();
            req.setToken("logged-out");

            when(loginCuentaService.autenticarJWT(any()))
                    .thenReturn(AutenticarJWTResponse.builder().valido(false).build());

            mockMvc.perform(post("/api/sesion/validar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido").value(false));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUT /api/sesion/correo
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/sesion/correo")
    class CambiarCorreo {

        @Test
        @DisplayName("200 OK al cambiar correo con datos válidos")
        void cambiarCorreoExitoso() throws Exception {
            CambiarCorreoRequest req = new CambiarCorreoRequest();
            req.setUsuarioId(1L);
            req.setNuevoCorreo("nuevo@eco.cl");
            req.setContrasenaActual("pass1234");

            when(loginCuentaService.cambiarCorreo(any()))
                    .thenReturn(MensajeResponse.de("Correo actualizado exitosamente."));

            mockMvc.perform(put("/api/sesion/correo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value("Correo actualizado exitosamente."));
        }

        @Test
        @DisplayName("409 CONFLICT cuando el nuevo correo ya está en uso")
        void cambiarCorreoDuplicado() throws Exception {
            CambiarCorreoRequest req = new CambiarCorreoRequest();
            req.setUsuarioId(1L);
            req.setNuevoCorreo("dup@eco.cl");
            req.setContrasenaActual("pass1234");

            when(loginCuentaService.cambiarCorreo(any()))
                    .thenThrow(new CorreoDuplicadoException("El correo 'dup@eco.cl' ya está en uso."));

            mockMvc.perform(put("/api/sesion/correo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isConflict());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUT /api/sesion/contrasena
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/sesion/contrasena")
    class CambiarContrasena {

        @Test
        @DisplayName("200 OK al cambiar contraseña correctamente")
        void cambiarContrasenaExitoso() throws Exception {
            CambiarContrasenaRequest req = new CambiarContrasenaRequest();
            req.setUsuarioId(1L);
            req.setContrasenaActual("vieja1234");
            req.setNuevaContrasena("nueva1234");

            when(loginCuentaService.cambiarContrasena(any()))
                    .thenReturn(MensajeResponse.de("Contraseña actualizada exitosamente."));

            mockMvc.perform(put("/api/sesion/contrasena")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada exitosamente."));
        }

        @Test
        @DisplayName("401 UNAUTHORIZED cuando la contraseña actual es incorrecta")
        void cambiarContrasenaActualIncorrecta() throws Exception {
            CambiarContrasenaRequest req = new CambiarContrasenaRequest();
            req.setUsuarioId(1L);
            req.setContrasenaActual("incorrecta");
            req.setNuevaContrasena("nueva1234");

            when(loginCuentaService.cambiarContrasena(any()))
                    .thenThrow(new AutenticacionException("Contraseña actual incorrecta."));

            mockMvc.perform(put("/api/sesion/contrasena")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/sesion/recuperar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/sesion/recuperar")
    class RecuperarCredenciales {

        @Test
        @DisplayName("200 OK al generar código de recuperación para correo existente")
        void recuperarExitoso() throws Exception {
            RecuperarCredencialesRequest req = new RecuperarCredencialesRequest();
            req.setCorreo("hocx@eco.cl");

            when(loginCuentaService.recuperarCredenciales(any()))
                    .thenReturn(MensajeResponse.de("Código de recuperación generado: ABC123"));

            mockMvc.perform(post("/api/sesion/recuperar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value("Código de recuperación generado: ABC123"));
        }

        @Test
        @DisplayName("404 NOT FOUND cuando el correo no existe")
        void recuperarCorreoInexistente() throws Exception {
            RecuperarCredencialesRequest req = new RecuperarCredencialesRequest();
            req.setCorreo("noexiste@eco.cl");

            when(loginCuentaService.recuperarCredenciales(any()))
                    .thenThrow(new CredencialNotFoundException("No existe una cuenta asociada a ese correo."));

            mockMvc.perform(post("/api/sesion/recuperar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/sesion/restablecer
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/sesion/restablecer")
    class RestablecerConToken {

        @Test
        @DisplayName("200 OK al restablecer contraseña con código válido")
        void restablecerExitoso() throws Exception {
            RestablecerConTokenRequest req = new RestablecerConTokenRequest();
            req.setCodigo("ABC123");
            req.setNuevaContrasena("nueva1234");

            when(loginCuentaService.restablecerConToken(any()))
                    .thenReturn(MensajeResponse.de("Contraseña restablecida exitosamente."));

            mockMvc.perform(post("/api/sesion/restablecer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value("Contraseña restablecida exitosamente."));
        }

        @Test
        @DisplayName("401 UNAUTHORIZED cuando el código expiró o ya fue usado")
        void restablecerCodigoExpirado() throws Exception {
            RestablecerConTokenRequest req = new RestablecerConTokenRequest();
            req.setCodigo("EXPIRED");
            req.setNuevaContrasena("nueva1234");

            when(loginCuentaService.restablecerConToken(any()))
                    .thenThrow(new TokenInvalidoException("El código es inválido, ya fue usado o expiró."));

            mockMvc.perform(post("/api/sesion/restablecer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DELETE /api/sesion/inhabilitar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/sesion/inhabilitar")
    class InhabilitarCredenciales {

        @Test
        @DisplayName("200 OK al inhabilitar usuario existente")
        void inhabilitarExitoso() throws Exception {
            InhabilitarCredencialesRequest req = new InhabilitarCredencialesRequest();
            req.setUsuarioId(9L);

            when(loginCuentaService.inhabilitarCredenciales(any()))
                    .thenReturn(MensajeResponse.de("Credenciales inhabilitadas. La cuenta ha sido bloqueada."));

            mockMvc.perform(delete("/api/sesion/inhabilitar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value("Credenciales inhabilitadas. La cuenta ha sido bloqueada."));
        }

        @Test
        @DisplayName("404 NOT FOUND cuando el usuario no existe")
        void inhabilitarUsuarioInexistente() throws Exception {
            InhabilitarCredencialesRequest req = new InhabilitarCredencialesRequest();
            req.setUsuarioId(999L);

            when(loginCuentaService.inhabilitarCredenciales(any()))
                    .thenThrow(new CredencialNotFoundException("No se encontraron credenciales para el usuario 999"));

            mockMvc.perform(delete("/api/sesion/inhabilitar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(req)))
                    .andExpect(status().isNotFound());
        }
    }
}