package com.horacio.ecomarket.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horacio.ecomarket.usuarios.model.EstadoPerfil;
import com.horacio.ecomarket.usuarios.model.Permiso;
import com.horacio.ecomarket.usuarios.model.Rol;
import com.horacio.ecomarket.usuarios.repository.EstadoPerfilRepository;
import com.horacio.ecomarket.usuarios.repository.PermisoRepository;
import com.horacio.ecomarket.usuarios.repository.RolRepository;
import com.horacio.ecomarket.usuarios.service.RegistroUsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración web para CatalogoController.
 * Cubre los 12 endpoints de Rol, Permiso y EstadoPerfil.
 *
 * Ejecutar:
 * mvn test -pl registro-usuarios-service -Dtest=CatalogoControllerTest
 */

@WebMvcTest(CatalogoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("CatalogoController")
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    // Necesario para que el contexto levante sin error (UsuarioController lo
    // requiere)
    @MockitoBean
    private RegistroUsuarioService registroUsuarioService;

    @MockitoBean
    private RolRepository rolRepository;
    @MockitoBean
    private PermisoRepository permisoRepository;
    @MockitoBean
    private EstadoPerfilRepository estadoPerfilRepository;

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ROLES
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Roles")
    class Roles {

        @Test
        @DisplayName("GET /api/usuarios/roles — 200 OK con lista de roles")
        void listarRoles() throws Exception {
            when(rolRepository.findAll()).thenReturn(List.of(
                    Rol.builder().id(1L).nombre("ADMIN").descripcion("Administrador").build(),
                    Rol.builder().id(2L).nombre("CLIENTE").descripcion("Cliente normal").build()));

            mockMvc.perform(get("/api/usuarios/roles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].nombre").value("ADMIN"))
                    .andExpect(jsonPath("$[1].nombre").value("CLIENTE"));
        }

        @Test
        @DisplayName("POST /api/usuarios/roles — 201 CREATED con el rol creado")
        void crearRol() throws Exception {
            Rol nuevo = Rol.builder().nombre("VENDEDOR").descripcion("Vendedor del marketplace").build();
            Rol guardado = Rol.builder().id(3L).nombre("VENDEDOR").descripcion("Vendedor del marketplace").build();

            when(rolRepository.save(any())).thenReturn(guardado);

            mockMvc.perform(post("/api/usuarios/roles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(nuevo)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.nombre").value("VENDEDOR"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/roles/{id} — 200 OK al actualizar rol existente")
        void actualizarRol() throws Exception {
            Rol existente = Rol.builder().id(1L).nombre("ADMIN").descripcion("Vieja desc").build();
            Rol datos = Rol.builder().nombre("SUPERADMIN").descripcion("Super administrador").build();

            when(rolRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(rolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(put("/api/usuarios/roles/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(datos)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("SUPERADMIN"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/roles/{id} — 400 BAD REQUEST cuando el rol no existe")
        void actualizarRolNoExiste() throws Exception {
            when(rolRepository.findById(999L))
                    .thenThrow(new RuntimeException("Rol no encontrado con ID: 999"));

            mockMvc.perform(put("/api/usuarios/roles/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Rol.builder().nombre("X").build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Rol no encontrado con ID: 999"));
        }

        @Test
        @DisplayName("DELETE /api/usuarios/roles/{id} — 204 NO CONTENT al eliminar")
        void eliminarRol() throws Exception {
            doNothing().when(rolRepository).deleteById(1L);

            mockMvc.perform(delete("/api/usuarios/roles/1"))
                    .andExpect(status().isNoContent());

            verify(rolRepository).deleteById(1L);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PERMISOS
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Permisos")
    class Permisos {

        @Test
        @DisplayName("GET /api/usuarios/permisos — 200 OK con lista de permisos")
        void listarPermisos() throws Exception {
            when(permisoRepository.findAll()).thenReturn(List.of(
                    Permiso.builder().id(1L).nombre("LEER_PRODUCTOS").build(),
                    Permiso.builder().id(2L).nombre("EDITAR_USUARIOS").build()));

            mockMvc.perform(get("/api/usuarios/permisos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("LEER_PRODUCTOS"))
                    .andExpect(jsonPath("$[1].nombre").value("EDITAR_USUARIOS"));
        }

        @Test
        @DisplayName("POST /api/usuarios/permisos — 201 CREATED con el permiso creado")
        void crearPermiso() throws Exception {
            Permiso nuevo = Permiso.builder().nombre("CREAR_PEDIDO").descripcion("Crear un pedido").build();
            Permiso guardado = Permiso.builder().id(5L).nombre("CREAR_PEDIDO").descripcion("Crear un pedido").build();

            when(permisoRepository.save(any())).thenReturn(guardado);

            mockMvc.perform(post("/api/usuarios/permisos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(nuevo)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.nombre").value("CREAR_PEDIDO"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/permisos/{id} — 200 OK al actualizar permiso")
        void actualizarPermiso() throws Exception {
            Permiso existente = Permiso.builder().id(1L).nombre("LEER").descripcion("Vieja").build();
            Permiso datos = Permiso.builder().nombre("LEER_TODO").descripcion("Leer todo").build();

            when(permisoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(permisoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(put("/api/usuarios/permisos/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(datos)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("LEER_TODO"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/permisos/{id} — 400 BAD REQUEST cuando no existe")
        void actualizarPermisoNoExiste() throws Exception {
            when(permisoRepository.findById(999L))
                    .thenThrow(new RuntimeException("Permiso no encontrado con ID: 999"));

            mockMvc.perform(put("/api/usuarios/permisos/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Permiso.builder().nombre("X").build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Permiso no encontrado con ID: 999"));
        }

        @Test
        @DisplayName("DELETE /api/usuarios/permisos/{id} — 204 NO CONTENT al eliminar")
        void eliminarPermiso() throws Exception {
            doNothing().when(permisoRepository).deleteById(2L);

            mockMvc.perform(delete("/api/usuarios/permisos/2"))
                    .andExpect(status().isNoContent());

            verify(permisoRepository).deleteById(2L);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ESTADOS DE PERFIL
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("EstadosPerfil")
    class EstadosPerfil {

        private EstadoPerfil estadoActivo() {
            EstadoPerfil e = new EstadoPerfil();
            e.setId(1L);
            e.setNombre("ACTIVO");
            return e;
        }

        @Test
        @DisplayName("GET /api/usuarios/estados-perfil — 200 OK con lista de estados")
        void listarEstadosPerfil() throws Exception {
            EstadoPerfil inactivo = new EstadoPerfil();
            inactivo.setId(2L);
            inactivo.setNombre("INACTIVO");

            when(estadoPerfilRepository.findAll()).thenReturn(List.of(estadoActivo(), inactivo));

            mockMvc.perform(get("/api/usuarios/estados-perfil"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("ACTIVO"))
                    .andExpect(jsonPath("$[1].nombre").value("INACTIVO"));
        }

        @Test
        @DisplayName("POST /api/usuarios/estados-perfil — 201 CREATED con el estado creado")
        void crearEstadoPerfil() throws Exception {
            EstadoPerfil nuevo = new EstadoPerfil();
            nuevo.setNombre("SUSPENDIDO");

            EstadoPerfil guardado = new EstadoPerfil();
            guardado.setId(3L);
            guardado.setNombre("SUSPENDIDO");

            when(estadoPerfilRepository.save(any())).thenReturn(guardado);

            mockMvc.perform(post("/api/usuarios/estados-perfil")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(nuevo)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.nombre").value("SUSPENDIDO"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/estados-perfil/{id} — 200 OK al actualizar estado")
        void actualizarEstadoPerfil() throws Exception {
            EstadoPerfil existente = estadoActivo();
            EstadoPerfil datos = new EstadoPerfil();
            datos.setNombre("BLOQUEADO");

            when(estadoPerfilRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(estadoPerfilRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(put("/api/usuarios/estados-perfil/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(datos)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("BLOQUEADO"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/estados-perfil/{id} — 400 BAD REQUEST cuando no existe")
        void actualizarEstadoNoExiste() throws Exception {
            when(estadoPerfilRepository.findById(999L))
                    .thenThrow(new RuntimeException("EstadoPerfil no encontrado con ID: 999"));

            EstadoPerfil datos = new EstadoPerfil();
            datos.setNombre("X");

            mockMvc.perform(put("/api/usuarios/estados-perfil/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(datos)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("EstadoPerfil no encontrado con ID: 999"));
        }

        @Test
        @DisplayName("DELETE /api/usuarios/estados-perfil/{id} — 204 NO CONTENT al eliminar")
        void eliminarEstadoPerfil() throws Exception {
            doNothing().when(estadoPerfilRepository).deleteById(1L);

            mockMvc.perform(delete("/api/usuarios/estados-perfil/1"))
                    .andExpect(status().isNoContent());

            verify(estadoPerfilRepository).deleteById(1L);
        }
    }
}