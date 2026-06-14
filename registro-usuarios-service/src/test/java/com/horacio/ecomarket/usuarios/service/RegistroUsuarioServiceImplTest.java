package com.horacio.ecomarket.usuarios.service;

import com.horacio.ecomarket.usuarios.model.Permiso;
import com.horacio.ecomarket.usuarios.model.PerfilUsuario;
import com.horacio.ecomarket.usuarios.model.Rol;
import com.horacio.ecomarket.usuarios.repository.PerfilUsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RegistroUsuarioServiceImpl.
 *
 * Ejecutar:
 *   mvn test -pl registro-usuarios-service -Dtest=RegistroUsuarioServiceImplTest
 *
 * BUGS CORREGIDOS vs versión anterior:
 *
 *  BUG 1 — Import de la propia clase eliminado:
 *    "import com.horacio.ecomarket.usuarios.service.RegistroUsuarioServiceImpl"
 *    ya está cubierto por estar en el mismo paquete. El import redundante causaba
 *    un warning de compilación que algunos IDEs/Maven tratan como error.
 *
 *  BUG 2 — new Rol(1L, "ADMIN") usaba constructor de 2 args pero Rol tiene 3 campos:
 *    Rol tiene (id, nombre, descripcion). @AllArgsConstructor genera
 *    new Rol(Long id, String nombre, String descripcion).
 *    new Rol(1L, "ADMIN") → error de compilación (2 args, necesita 3).
 *    Lo mismo para new Permiso(1L, "VER_PRODUCTOS").
 *    FIX: se usa @Builder de Lombok que sí está declarado en ambas clases,
 *    pasando solo los campos necesarios y dejando descripcion en null.
 *
 *  BUG 3 — @InjectMocks + @BeforeEach hacían double-init:
 *    La versión anterior tenía AMBOS @InjectMocks y un @BeforeEach que
 *    sobreescribía el service con new RegistroUsuarioServiceImpl(...).
 *    Eso es redundante pero no peligroso porque el constructor de la nueva
 *    instancia recibe los mismos mocks. Sin embargo, si Mockito procesa
 *    @InjectMocks DESPUÉS del @BeforeEach, el service queda con la instancia
 *    de @InjectMocks (sin los mocks bien inyectados). Para evitar ambigüedad
 *    se eliminó @InjectMocks y se construye únicamente en @BeforeEach,
 *    que es el patrón correcto para constructor injection.
 */
@ExtendWith(MockitoExtension.class)
class RegistroUsuarioServiceImplTest {

    // BUG 3 FIX: solo @Mock, sin @InjectMocks
    @Mock private PerfilUsuarioRepository repository;
    @Mock private RestTemplate             restTemplate;

    private RegistroUsuarioServiceImpl service;

    // BUG 3 FIX: construcción manual en @BeforeEach — correcto para constructor injection
    @BeforeEach
    void setup() {
        service = new RegistroUsuarioServiceImpl(repository, restTemplate);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PerfilUsuario perfilNuevo(String nombre, String correo) {
        return PerfilUsuario.builder()
                .nombre(nombre)
                .correo(correo)
                .telefono("912345678")
                .build();
    }

    private PerfilUsuario perfilGuardado(Long id, String nombre, String correo) {
        return PerfilUsuario.builder()
                .id(id)
                .nombre(nombre)
                .correo(correo)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // registrarCuenta
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("registrarCuenta")
    class RegistrarCuenta {

        @Test
        @DisplayName("registra usuario exitosamente y persiste en repositorio")
        void registraUsuarioExitosamente() {
            PerfilUsuario perfil = perfilNuevo("Ana López", "ana@eco.cl");

            when(repository.findByCorreo("ana@eco.cl")).thenReturn(Optional.empty());
            when(repository.save(any(PerfilUsuario.class))).thenAnswer(i -> {
                PerfilUsuario p = i.getArgument(0);
                return PerfilUsuario.builder()
                        .id(1L)
                        .nombre(p.getNombre())
                        .correo(p.getCorreo())
                        .fechaCreacion(p.getFechaCreacion())
                        .build();
            });
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok("ok"));

            PerfilUsuario resultado = service.registrarCuenta(perfil, "pass123");

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getCorreo()).isEqualTo("ana@eco.cl");
            verify(repository).save(argThat(p -> p.getFechaCreacion() != null));
        }

        @Test
        @DisplayName("lanza RuntimeException si el correo ya está registrado")
        void correoDuplicadoLanzaExcepcion() {
            PerfilUsuario existente = perfilGuardado(1L, "Ana", "ana@eco.cl");
            when(repository.findByCorreo("ana@eco.cl")).thenReturn(Optional.of(existente));

            PerfilUsuario perfil = perfilNuevo("Ana Duplicada", "ana@eco.cl");

            assertThatThrownBy(() -> service.registrarCuenta(perfil, "pass"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ana@eco.cl");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("lanza RuntimeException si iniciosesion-service falla al crear credenciales")
        void falloEnIniciosesionLanzaExcepcion() {
            PerfilUsuario perfil = perfilNuevo("Pedro", "pedro@eco.cl");
            when(repository.findByCorreo("pedro@eco.cl")).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(i -> {
                PerfilUsuario p = i.getArgument(0);
                return PerfilUsuario.builder()
                        .id(2L).nombre(p.getNombre()).correo(p.getCorreo()).build();
            });
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> service.registrarCuenta(perfil, "pass123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("credenciales");
        }

        @Test
        @DisplayName("el rol del perfil se incluye como ROLE_X en la request a iniciosesion-service")
        void rolSeMapeoCorrectamente() {
            // BUG 2 FIX: Rol tiene 3 campos (id, nombre, descripcion) → usar builder
            Rol rol = Rol.builder().id(1L).nombre("ADMIN").build();
            PerfilUsuario perfil = perfilNuevo("Boss", "boss@eco.cl");
            perfil.setRol(rol);

            when(repository.findByCorreo("boss@eco.cl")).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(i -> {
                PerfilUsuario p = i.getArgument(0);
                return PerfilUsuario.builder()
                        .id(3L).nombre(p.getNombre())
                        .correo(p.getCorreo()).rol(p.getRol()).build();
            });
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok("ok"));

            service.registrarCuenta(perfil, "pass123");

            verify(restTemplate).postForEntity(
                    contains("credencial"),
                    argThat(body -> body.toString().contains("ROLE_ADMIN")),
                    eq(String.class)
            );
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarPorId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("retorna el perfil cuando el ID existe")
        void retornaPerfilExistente() {
            PerfilUsuario perfil = perfilGuardado(5L, "Carlos", "carlos@eco.cl");
            when(repository.findById(5L)).thenReturn(Optional.of(perfil));

            PerfilUsuario resultado = service.buscarPorId(5L);

            assertThat(resultado.getId()).isEqualTo(5L);
            assertThat(resultado.getNombre()).isEqualTo("Carlos");
        }

        @Test
        @DisplayName("lanza RuntimeException si el ID no existe")
        void idInexistenteLanzaExcepcion() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarPorCorreo
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarPorCorreo")
    class BuscarPorCorreo {

        @Test
        @DisplayName("retorna el perfil cuando el correo existe")
        void retornaPerfilPorCorreo() {
            PerfilUsuario perfil = perfilGuardado(1L, "Ana", "ana@eco.cl");
            when(repository.findByCorreo("ana@eco.cl")).thenReturn(Optional.of(perfil));

            PerfilUsuario resultado = service.buscarPorCorreo("ana@eco.cl");

            assertThat(resultado.getCorreo()).isEqualTo("ana@eco.cl");
        }

        @Test
        @DisplayName("lanza RuntimeException si el correo no existe")
        void correoInexistenteLanzaExcepcion() {
            when(repository.findByCorreo("none@eco.cl")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorCorreo("none@eco.cl"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("none@eco.cl");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // modificarDatosUsuario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("modificarDatosUsuario")
    class ModificarDatosUsuario {

        @Test
        @DisplayName("actualiza nombre y teléfono correctamente")
        void actualizaNombreYTelefono() {
            PerfilUsuario existente = perfilGuardado(1L, "Ana", "ana@eco.cl");
            existente.setTelefono("912345678");

            when(repository.findById(1L)).thenReturn(Optional.of(existente));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

            PerfilUsuario datosNuevos = new PerfilUsuario();
            datosNuevos.setNombre("Ana Modificada");
            datosNuevos.setTelefono("999999999");
            datosNuevos.setCorreo("ana@eco.cl"); // mismo correo, no cambia

            PerfilUsuario resultado = service.modificarDatosUsuario(1L, datosNuevos);

            assertThat(resultado.getNombre()).isEqualTo("Ana Modificada");
            assertThat(resultado.getTelefono()).isEqualTo("999999999");
        }

        @Test
        @DisplayName("lanza RuntimeException si el nuevo correo ya está en uso por otro usuario")
        void correoNuevoDuplicadoLanzaExcepcion() {
            PerfilUsuario existente = perfilGuardado(1L, "Ana", "ana@eco.cl");
            PerfilUsuario otroPerfil = perfilGuardado(2L, "Otro", "nuevo@eco.cl");

            when(repository.findById(1L)).thenReturn(Optional.of(existente));
            when(repository.findByCorreo("nuevo@eco.cl")).thenReturn(Optional.of(otroPerfil));

            PerfilUsuario datosNuevos = new PerfilUsuario();
            datosNuevos.setNombre("Ana");
            datosNuevos.setCorreo("nuevo@eco.cl");

            assertThatThrownBy(() -> service.modificarDatosUsuario(1L, datosNuevos))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("nuevo@eco.cl");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // eliminarUsuario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("eliminarUsuario")
    class EliminarUsuario {

        @Test
        @DisplayName("elimina el usuario y retorna true")
        void eliminaUsuarioExitosamente() {
            PerfilUsuario perfil = perfilGuardado(1L, "Ana", "ana@eco.cl");
            when(repository.findById(1L)).thenReturn(Optional.of(perfil));

            Boolean resultado = service.eliminarUsuario(1L);

            assertThat(resultado).isTrue();
            verify(repository).delete(perfil);
        }

        @Test
        @DisplayName("lanza RuntimeException si el usuario a eliminar no existe")
        void usuarioInexistenteLanzaExcepcion() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.eliminarUsuario(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarUsuarios / listarPorRol
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarUsuarios y listarPorRol")
    class Listados {

        @Test
        @DisplayName("listarUsuarios retorna todos los perfiles")
        void listarUsuariosRetornaTodos() {
            List<PerfilUsuario> perfiles = List.of(
                    perfilGuardado(1L, "Ana", "ana@eco.cl"),
                    perfilGuardado(2L, "Bob", "bob@eco.cl")
            );
            when(repository.findAll()).thenReturn(perfiles);

            List<PerfilUsuario> resultado = service.listarUsuarios();

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("listarPorRol delega en el repositorio con el rol correcto")
        void listarPorRolDelegaEnRepository() {
            // BUG 2 FIX: Rol.builder() en lugar de new Rol(1L, "USER")
            Rol rol = Rol.builder().id(1L).nombre("USER").build();
            List<PerfilUsuario> perfiles = List.of(perfilGuardado(1L, "Ana", "ana@eco.cl"));
            when(repository.findByRol(rol)).thenReturn(perfiles);

            List<PerfilUsuario> resultado = service.listarPorRol(rol);

            assertThat(resultado).hasSize(1);
            verify(repository).findByRol(rol);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // configurarPermisos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("configurarPermisos")
    class ConfigurarPermisos {

        @Test
        @DisplayName("reemplaza los permisos del usuario y retorna true")
        void configurarPermisosExitosamente() {
            PerfilUsuario perfil = perfilGuardado(1L, "Ana", "ana@eco.cl");
            when(repository.findById(1L)).thenReturn(Optional.of(perfil));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

            // BUG 2 FIX: Permiso también tiene 3 campos (id, nombre, descripcion) → builder
            List<Permiso> nuevosPermisos = List.of(
                    Permiso.builder().id(1L).nombre("VER_PRODUCTOS").build(),
                    Permiso.builder().id(2L).nombre("CREAR_PEDIDO").build()
            );

            Boolean resultado = service.configurarPermisos(1L, nuevosPermisos);

            assertThat(resultado).isTrue();
            verify(repository).save(argThat(p -> p.getPermisos().size() == 2));
        }
    }
}