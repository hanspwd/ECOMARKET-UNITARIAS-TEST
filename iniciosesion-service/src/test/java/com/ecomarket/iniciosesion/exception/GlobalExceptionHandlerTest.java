package com.ecomarket.iniciosesion.exception;

import com.ecomarket.iniciosesion.controller.LoginCuentaController;
import com.ecomarket.iniciosesion.dto.*;
import com.ecomarket.iniciosesion.service.LoginCuentaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para GlobalExceptionHandler.
 *
 * Estrategia: usamos MockMvc para disparar requests al controller real,
 * hacemos que el service (mockeado) lance cada excepción, y verificamos
 * que el handler devuelva el status HTTP y el body JSON correctos.
 *
 * Ejecutar:
 *   mvn test -pl iniciosesion-service -Dtest=GlobalExceptionHandlerTest
 */
@WebMvcTest(LoginCuentaController.class)
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LoginCuentaService loginCuentaService;

    // ── Helper: request mínimo para POST /api/sesion/login ───────────────────
    private String loginJson() throws Exception {
        IniciarSesionRequest req = new IniciarSesionRequest();
        req.setCorreo("test@eco.cl");
        req.setContrasena("pass1234");
        return objectMapper.writeValueAsString(req);
    }

    // ── Helper: request mínimo para POST /api/sesion/credencial ─────────────
    private String credencialJson() throws Exception {
        CrearCredencialRequest req = new CrearCredencialRequest();
        req.setUsuarioId(1L);
        req.setCorreo("test@eco.cl");
        req.setContrasena("pass1234");
        return objectMapper.writeValueAsString(req);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 400 BAD REQUEST — validación de campos (@Valid)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("400 BAD REQUEST — MethodArgumentNotValidException")
    class Validacion {

        @Test
        @DisplayName("devuelve 400 y campo 'details' con los errores de validación")
        void campoObligatorioFaltante() throws Exception {
            // Request sin correo ni contraseña → falla @NotBlank
            IniciarSesionRequest req = new IniciarSesionRequest();
            // correo y contrasena quedan null → viola @NotBlank

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.details").isMap());
        }

        @Test
        @DisplayName("devuelve 400 cuando el correo tiene formato inválido")
        void correoFormatoInvalido() throws Exception {
            IniciarSesionRequest req = new IniciarSesionRequest();
            req.setCorreo("no-es-un-correo");
            req.setContrasena("pass1234");

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.correo").exists());
        }

        @Test
        @DisplayName("devuelve 400 cuando la contraseña tiene menos de 8 caracteres")
        void contrasenaDemasiadoCorta() throws Exception {
            CrearCredencialRequest req = new CrearCredencialRequest();
            req.setUsuarioId(1L);
            req.setCorreo("test@eco.cl");
            req.setContrasena("123");  // menos de 8 chars → viola @Size(min=8)

            mockMvc.perform(post("/api/sesion/credencial")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.contrasena").exists());
        }

        @Test
        @DisplayName("el body de validación incluye path, timestamp y message genérico")
        void bodyValidacionTieneEstructuraCompleta() throws Exception {
            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new IniciarSesionRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/api/sesion/login"))
                    .andExpect(jsonPath("$.message").value("La validación de los datos ha fallado. Revisa los detalles."));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 401 UNAUTHORIZED — AutenticacionException
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("401 UNAUTHORIZED — AutenticacionException")
    class Autenticacion {

        @Test
        @DisplayName("devuelve 401 con el mensaje de la excepción en el body")
        void autenticacionExceptionDevuelve401() throws Exception {
            when(loginCuentaService.iniciarSesion(any()))
                    .thenThrow(new AutenticacionException("Correo o contraseña incorrectos."));

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Correo o contraseña incorrectos."));
        }

        @Test
        @DisplayName("el body incluye el path de la request original")
        void autenticacionExceptionIncludeePath() throws Exception {
            when(loginCuentaService.iniciarSesion(any()))
                    .thenThrow(new AutenticacionException("Error de auth."));

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson()))
                    .andExpect(jsonPath("$.path").value("/api/sesion/login"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 401 UNAUTHORIZED — TokenInvalidoException
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("401 UNAUTHORIZED — TokenInvalidoException")
    class TokenInvalido {

        @Test
        @DisplayName("devuelve 401 con mensaje del token inválido")
        void tokenInvalidoDevuelve401() throws Exception {
            CerrarSesionRequest req = new CerrarSesionRequest();
            req.setToken("token-malo");

            when(loginCuentaService.cerrarSesion(any()))
                    .thenThrow(new TokenInvalidoException("El token proporcionado no es válido."));

            mockMvc.perform(post("/api/sesion/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("El token proporcionado no es válido."));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 403 FORBIDDEN — CuentaBloqueadaException
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("403 FORBIDDEN — CuentaBloqueadaException")
    class CuentaBloqueada {

        @Test
        @DisplayName("devuelve 403 cuando la cuenta está bloqueada")
        void cuentaBloqueadaDevuelve403() throws Exception {
            when(loginCuentaService.iniciarSesion(any()))
                    .thenThrow(new CuentaBloqueadaException("La cuenta está bloqueada. Contacte al administrador."));

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("La cuenta está bloqueada. Contacte al administrador."));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 404 NOT FOUND — CredencialNotFoundException
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("404 NOT FOUND — CredencialNotFoundException")
    class CredencialNotFound {

        @Test
        @DisplayName("devuelve 404 con el mensaje cuando no existe la credencial")
        void credencialNotFoundDevuelve404() throws Exception {
            RecuperarCredencialesRequest req = new RecuperarCredencialesRequest();
            req.setCorreo("noexiste@eco.cl");

            when(loginCuentaService.recuperarCredenciales(any()))
                    .thenThrow(new CredencialNotFoundException("No existe una cuenta asociada a ese correo."));

            mockMvc.perform(post("/api/sesion/recuperar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("No existe una cuenta asociada a ese correo."));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 409 CONFLICT — CorreoDuplicadoException
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("409 CONFLICT — CorreoDuplicadoException")
    class CorreoDuplicado {

        @Test
        @DisplayName("devuelve 409 cuando el correo ya está registrado")
        void correoDuplicadoDevuelve409() throws Exception {
            when(loginCuentaService.crearCredencial(any()))
                    .thenThrow(new CorreoDuplicadoException("El correo 'dup@eco.cl' ya está registrado."));

            mockMvc.perform(post("/api/sesion/credencial")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(credencialJson()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("El correo 'dup@eco.cl' ya está registrado."));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 409 CONFLICT — DataIntegrityViolationException
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("409 CONFLICT — DataIntegrityViolationException")
    class DataIntegrity {

        @Test
        @DisplayName("devuelve 409 con mensaje genérico de integridad de BD")
        void dataIntegrityDevuelve409() throws Exception {
            when(loginCuentaService.crearCredencial(any()))
                    .thenThrow(new DataIntegrityViolationException("constraint violation"));

            mockMvc.perform(post("/api/sesion/credencial")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(credencialJson()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Error de integridad en la base de datos."));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 400 BAD REQUEST — RuntimeException genérica
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("400 BAD REQUEST — RuntimeException genérica")
    class RuntimeExcepcionGenerica {

        @Test
        @DisplayName("devuelve 400 para cualquier RuntimeException no mapeada")
        void runtimeExceptionDevuelve400() throws Exception {
            when(loginCuentaService.iniciarSesion(any()))
                    .thenThrow(new RuntimeException("Error inesperado de negocio."));

            mockMvc.perform(post("/api/sesion/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Error inesperado de negocio."));
        }
    }
    // ═════════════════════════════════════════════════════════════════════════
    // 500 INTERNAL SERVER ERROR — Exception Genérica
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("500 INTERNAL SERVER ERROR — Exception General")
    class ErrorInterno {

        @Test
        @DisplayName("devuelve 500 con mensaje genérico ante cualquier excepción no controlada")
        void exceptionGeneralDevuelve500() {
            // 1. ARRANGE: Instanciamos el handler directamente sin usar MockMvc
            GlobalExceptionHandler handler = new GlobalExceptionHandler();
            
            // Creamos una excepción genérica cualquiera
            Exception excepcionCualquiera = new Exception("Fallo catastrófico del sistema (ej: se quemó la base de datos)");
            
            // Creamos un request HTTP falso usando la clase que nos da Spring
            org.springframework.mock.web.MockHttpServletRequest requestFalso = 
                    new org.springframework.mock.web.MockHttpServletRequest();
            requestFalso.setRequestURI("/api/sistema/explotar");

            // 2. ACT: Ejecutamos el método directamente pasándole la excepción y el request falso
            org.springframework.http.ResponseEntity<ErrorResponseDTO> response = 
                    handler.handleGeneralException(excepcionCualquiera, requestFalso);

            // 3. ASSERT: Validamos que devuelva el código 500 y oculte el error real
            org.junit.jupiter.api.Assertions.assertEquals(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, 
                response.getStatusCode()
            );
            
            ErrorResponseDTO body = response.getBody();
            org.junit.jupiter.api.Assertions.assertNotNull(body);
            org.junit.jupiter.api.Assertions.assertEquals("Ocurrió un error inesperado en el servidor.", body.getMessage());
            org.junit.jupiter.api.Assertions.assertEquals("/api/sistema/explotar", body.getPath());
        }
    }
}