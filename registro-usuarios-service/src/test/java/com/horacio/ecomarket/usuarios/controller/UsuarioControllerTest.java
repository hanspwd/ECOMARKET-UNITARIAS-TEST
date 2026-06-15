package com.horacio.ecomarket.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horacio.ecomarket.usuarios.dto.ConfigurarPermisosDTO;
import com.horacio.ecomarket.usuarios.dto.ModificarUsuarioDTO;
import com.horacio.ecomarket.usuarios.dto.RegistroUsuarioDTO;
import com.horacio.ecomarket.usuarios.model.EstadoPerfil;
import com.horacio.ecomarket.usuarios.model.PerfilUsuario;
import com.horacio.ecomarket.usuarios.model.Rol;
import com.horacio.ecomarket.usuarios.repository.EstadoPerfilRepository;
import com.horacio.ecomarket.usuarios.repository.PermisoRepository;
import com.horacio.ecomarket.usuarios.repository.RolRepository;
import com.horacio.ecomarket.usuarios.service.RegistroUsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración web para UsuarioController.
 * MockMvc + service mockeado — sin MySQL, sin RestTemplate real.
 *
 * Ejecutar:
 *   mvn test -pl registro-usuarios-service -Dtest=UsuarioControllerTest
 */

@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistroUsuarioService service;

    @MockitoBean
    private RolRepository rolRepository;

    @MockitoBean
    private PermisoRepository permisoRepository;

    @MockitoBean
    private EstadoPerfilRepository estadoPerfilRepository;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Rol rolCliente() {
        return Rol.builder().id(1L).nombre("CLIENTE").build();
    }

    private PerfilUsuario perfilGuardado() {
        return PerfilUsuario.builder()
                .id(1L)
                .nombre("Horacio Navarrete")
                .correo("hocx@eco.cl")
                .telefono("+56912345678")
                .rol(rolCliente())
                .build();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /api/usuarios/registro
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/usuarios/registro")
    class Registro {

        @Test
        @DisplayName("201 CREATED con el perfil creado en el body")
        void registroExitoso() throws Exception {
            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setNombre("Horacio Navarrete");
            dto.setCorreo("hocx@eco.cl");
            dto.setContrasenaInicial("pass123");
            dto.setRolId(1L);

            when(rolRepository.findById(1L)).thenReturn(Optional.of(rolCliente()));
            when(service.registrarCuenta(any(), eq("pass123"))).thenReturn(perfilGuardado());

            mockMvc.perform(post("/api/usuarios/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.correo").value("hocx@eco.cl"));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando falta el nombre")
        void registroSinNombre() throws Exception {
            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setCorreo("hocx@eco.cl");
            dto.setContrasenaInicial("pass123");

            mockMvc.perform(post("/api/usuarios/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando el correo tiene formato inválido")
        void registroCorreoInvalido() throws Exception {
            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setNombre("Horacio");
            dto.setCorreo("no-es-correo");
            dto.setContrasenaInicial("pass123");

            mockMvc.perform(post("/api/usuarios/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando la contraseña tiene menos de 5 caracteres")
        void registroContrasenaMuyCorta() throws Exception {
            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setNombre("Horacio");
            dto.setCorreo("hocx@eco.cl");
            dto.setContrasenaInicial("123");

            mockMvc.perform(post("/api/usuarios/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando el correo ya está registrado")
        void registroCorreoDuplicado() throws Exception {
            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setNombre("Horacio");
            dto.setCorreo("dup@eco.cl");
            dto.setContrasenaInicial("pass123");

            when(service.registrarCuenta(any(), anyString()))
                    .thenThrow(new RuntimeException("El correo ya está registrado: dup@eco.cl"));

            mockMvc.perform(post("/api/usuarios/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El correo ya está registrado: dup@eco.cl"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUT /api/usuarios/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/usuarios/{id}")
    class Modificar {

        @Test
        @DisplayName("200 OK con el perfil actualizado")
        void modificarExitoso() throws Exception {
            ModificarUsuarioDTO dto = new ModificarUsuarioDTO();
            dto.setNombre("Horacio Nuevo");
            dto.setCorreo("hocx@eco.cl");

            when(service.modificarDatosUsuario(eq(1L), any())).thenReturn(perfilGuardado());

            mockMvc.perform(put("/api/usuarios/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando usuario no existe")
        void modificarUsuarioInexistente() throws Exception {
            ModificarUsuarioDTO dto = new ModificarUsuarioDTO();
            dto.setNombre("X");
            dto.setCorreo("x@eco.cl");

            when(service.modificarDatosUsuario(eq(999L), any()))
                    .thenThrow(new RuntimeException("Usuario no encontrado con ID: 999"));

            mockMvc.perform(put("/api/usuarios/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Usuario no encontrado con ID: 999"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /api/usuarios
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/usuarios")
    class ListarTodos {

        @Test
        @DisplayName("200 OK con lista de usuarios")
        void listarTodosExitoso() throws Exception {
            when(service.listarUsuarios()).thenReturn(List.of(perfilGuardado()));

            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].correo").value("hocx@eco.cl"));
        }

        @Test
        @DisplayName("200 OK con lista vacía cuando no hay usuarios")
        void listarTodosVacio() throws Exception {
            when(service.listarUsuarios()).thenReturn(List.of());

            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /api/usuarios/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/usuarios/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("200 OK cuando el usuario existe")
        void buscarPorIdExitoso() throws Exception {
            when(service.buscarPorId(1L)).thenReturn(perfilGuardado());

            mockMvc.perform(get("/api/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Horacio Navarrete"));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando el usuario no existe")
        void buscarPorIdNoExiste() throws Exception {
            when(service.buscarPorId(404L))
                    .thenThrow(new RuntimeException("Usuario no encontrado con ID: 404"));

            mockMvc.perform(get("/api/usuarios/404"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Usuario no encontrado con ID: 404"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /api/usuarios/correo/{correo}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/usuarios/correo/{correo}")
    class BuscarPorCorreo {

        @Test
        @DisplayName("200 OK cuando el correo existe")
        void buscarPorCorreoExitoso() throws Exception {
            when(service.buscarPorCorreo("hocx@eco.cl")).thenReturn(perfilGuardado());

            mockMvc.perform(get("/api/usuarios/correo/hocx@eco.cl"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.correo").value("hocx@eco.cl"));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando el correo no existe")
        void buscarPorCorreoNoExiste() throws Exception {
            when(service.buscarPorCorreo("x@eco.cl"))
                    .thenThrow(new RuntimeException("Usuario no encontrado con correo: x@eco.cl"));

            mockMvc.perform(get("/api/usuarios/correo/x@eco.cl"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Usuario no encontrado con correo: x@eco.cl"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /api/usuarios/rol/{rolId}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/usuarios/rol/{rolId}")
    class ListarPorRol {

        @Test
        @DisplayName("200 OK con lista de usuarios del rol")
        void listarPorRolExitoso() throws Exception {
            when(rolRepository.findById(1L)).thenReturn(Optional.of(rolCliente()));
            when(service.listarPorRol(any())).thenReturn(List.of(perfilGuardado()));

            mockMvc.perform(get("/api/usuarios/rol/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].correo").value("hocx@eco.cl"));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando el rol no existe")
        void listarPorRolNoExiste() throws Exception {
            when(rolRepository.findById(999L)).thenThrow(new RuntimeException("Rol no encontrado con ID: 999"));

            mockMvc.perform(get("/api/usuarios/rol/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Rol no encontrado con ID: 999"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUT /api/usuarios/{id}/permisos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/usuarios/{id}/permisos")
    class ConfigurarPermisos {

        @Test
        @DisplayName("200 OK retorna true al configurar permisos")
        void configurarPermisosExitoso() throws Exception {
            ConfigurarPermisosDTO dto = new ConfigurarPermisosDTO();
            dto.setPermisoIds(List.of(1L, 2L));

            when(permisoRepository.findById(1L))
                    .thenReturn(Optional.of(com.horacio.ecomarket.usuarios.model.Permiso.builder()
                            .id(1L).nombre("LEER_PRODUCTOS").build()));
            when(permisoRepository.findById(2L))
                    .thenReturn(Optional.of(com.horacio.ecomarket.usuarios.model.Permiso.builder()
                            .id(2L).nombre("EDITAR_USUARIOS").build()));
            when(service.configurarPermisos(eq(1L), any())).thenReturn(true);

            mockMvc.perform(put("/api/usuarios/1/permisos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando permisoIds es nulo")
        void configurarPermisosNulos() throws Exception {
            ConfigurarPermisosDTO dto = new ConfigurarPermisosDTO();
            // permisoIds queda null → viola @NotNull

            mockMvc.perform(put("/api/usuarios/1/permisos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DELETE /api/usuarios/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/usuarios/{id}")
    class EliminarUsuario {

        @Test
        @DisplayName("200 OK retorna true al eliminar usuario existente")
        void eliminarExitoso() throws Exception {
            when(service.eliminarUsuario(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));
        }

        @Test
        @DisplayName("400 BAD REQUEST cuando el usuario a eliminar no existe")
        void eliminarNoExiste() throws Exception {
            when(service.eliminarUsuario(999L))
                    .thenThrow(new RuntimeException("Usuario no encontrado con ID: 999"));

            mockMvc.perform(delete("/api/usuarios/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Usuario no encontrado con ID: 999"));
        }
    }
}