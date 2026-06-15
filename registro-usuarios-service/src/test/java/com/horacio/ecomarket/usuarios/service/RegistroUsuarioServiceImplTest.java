package com.horacio.ecomarket.usuarios.service;

import com.horacio.ecomarket.usuarios.model.EstadoPerfil;
import com.horacio.ecomarket.usuarios.model.PerfilUsuario;
import com.horacio.ecomarket.usuarios.model.Permiso;
import com.horacio.ecomarket.usuarios.model.Rol;
import com.horacio.ecomarket.usuarios.repository.PerfilUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RegistroUsuarioServiceImpl.
 * Sin Spring, sin BD, sin RestTemplate real — todo mockeado.
 *
 * Ejecutar:
 *   mvn test -pl registro-usuarios-service -Dtest=RegistroUsuarioServiceImplTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistroUsuarioServiceImpl")
class RegistroUsuarioServiceImplTest {

    @Mock
    private PerfilUsuarioRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RegistroUsuarioServiceImpl service;

    // ── Fixtures reutilizables ────────────────────────────────────────────────

    private Rol rolCliente;
    private EstadoPerfil estadoActivo;
    private PerfilUsuario perfilBase;

    @BeforeEach
    void setUp() {
        rolCliente = Rol.builder().id(1L).nombre("CLIENTE").descripcion("Cliente normal").build();
        estadoActivo = new EstadoPerfil();
        estadoActivo.setId(1L);
        estadoActivo.setNombre("ACTIVO");

        perfilBase = PerfilUsuario.builder()
                .nombre("Horacio Navarrete")
                .correo("hocx@eco.cl")
                .telefono("+56912345678")
                .rol(rolCliente)
                .estadoPerfil(estadoActivo)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // registrarCuenta
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("registrarCuenta")
    class RegistrarCuenta {

        @Test
        @DisplayName("registra usuario correctamente cuando el correo no existe")
        void registraUsuarioExitosamente() {
            when(repository.findByCorreo("hocx@eco.cl")).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> {
                PerfilUsuario p = inv.getArgument(0);
                p = PerfilUsuario.builder()
                        .id(1L)
                        .nombre(p.getNombre())
                        .correo(p.getCorreo())
                        .telefono(p.getTelefono())
                        .rol(p.getRol())
                        .fechaCreacion(p.getFechaCreacion())
                        .build();
                return p;
            });
            // RestTemplate no lanza excepción → simulamos llamada exitosa
            when(restTemplate.postForEntity(contains("8086"), any(), eq(String.class)))
                    .thenReturn(null);

            PerfilUsuario resultado = service.registrarCuenta(perfilBase, "pass123");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getCorreo()).isEqualTo("hocx@eco.cl");
            verify(repository).save(any(PerfilUsuario.class));
        }

        @Test
        @DisplayName("asigna fechaCreacion automáticamente al registrar")
        void asignaFechaCreacion() {
            when(repository.findByCorreo(anyString())).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);

            PerfilUsuario resultado = service.registrarCuenta(perfilBase, "pass123");

            assertThat(resultado.getFechaCreacion()).isNotNull();
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el correo ya está registrado")
        void lanzaExcepcionCorreoDuplicado() {
            when(repository.findByCorreo("hocx@eco.cl")).thenReturn(Optional.of(perfilBase));

            assertThatThrownBy(() -> service.registrarCuenta(perfilBase, "pass123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("hocx@eco.cl");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("lanza RuntimeException cuando iniciosesion-service falla")
        void lanzaExcepcionSiIniciosesionFalla() {
            when(repository.findByCorreo(anyString())).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> {
                PerfilUsuario p = inv.getArgument(0);
                p.setId(99L);
                return p;
            });
            when(restTemplate.postForEntity(contains("8086"), any(), eq(String.class)))
                    .thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> service.registrarCuenta(perfilBase, "pass123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al crear credenciales");
        }

        @Test
        @DisplayName("usa ROLE_USER como rol por defecto cuando el perfil no tiene rol")
        void usaRolUserPorDefectoCuandoNoHayRol() {
            PerfilUsuario sinRol = PerfilUsuario.builder()
                    .nombre("Sin Rol")
                    .correo("sinrol@eco.cl")
                    .build();

            when(repository.findByCorreo("sinrol@eco.cl")).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> {
                PerfilUsuario p = inv.getArgument(0);
                p.setId(2L);
                return p;
            });

            // Capturamos el body enviado al restTemplate para verificar rol
            when(restTemplate.postForEntity(contains("8086"), any(), eq(String.class)))
                    .thenReturn(null);

            service.registrarCuenta(sinRol, "pass123");

            // El servicio no lanza excepción → flujo completado con ROLE_USER
            verify(restTemplate).postForEntity(contains("8086"), any(), eq(String.class));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // modificarDatosUsuario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("modificarDatosUsuario")
    class ModificarDatosUsuario {

        @Test
        @DisplayName("modifica nombre y teléfono correctamente")
        void modificaNombreYTelefono() {
            PerfilUsuario existente = PerfilUsuario.builder()
                    .id(1L).nombre("Viejo").correo("hocx@eco.cl").telefono("111").build();

            PerfilUsuario datosNuevos = PerfilUsuario.builder()
                    .nombre("Horacio Nuevo")
                    .correo("hocx@eco.cl") // mismo correo → no cambia
                    .telefono("+56999999999")
                    .build();

            when(repository.findById(1L)).thenReturn(Optional.of(existente));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PerfilUsuario resultado = service.modificarDatosUsuario(1L, datosNuevos);

            assertThat(resultado.getNombre()).isEqualTo("Horacio Nuevo");
            assertThat(resultado.getTelefono()).isEqualTo("+56999999999");
        }

        @Test
        @DisplayName("actualiza correo cuando cambia y no está en uso")
        void actualizaCorreoCuandoNuevoCorreoEsLibre() {
            PerfilUsuario existente = PerfilUsuario.builder()
                    .id(1L).nombre("Horacio").correo("viejo@eco.cl").build();

            PerfilUsuario datosNuevos = PerfilUsuario.builder()
                    .nombre("Horacio").correo("nuevo@eco.cl").build();

            when(repository.findById(1L)).thenReturn(Optional.of(existente));
            when(repository.findByCorreo("nuevo@eco.cl")).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PerfilUsuario resultado = service.modificarDatosUsuario(1L, datosNuevos);

            assertThat(resultado.getCorreo()).isEqualTo("nuevo@eco.cl");
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el nuevo correo ya está en uso")
        void lanzaExcepcionCorreoNuevoEnUso() {
            PerfilUsuario existente = PerfilUsuario.builder()
                    .id(1L).nombre("Horacio").correo("viejo@eco.cl").build();

            PerfilUsuario datosNuevos = PerfilUsuario.builder()
                    .nombre("Horacio").correo("ocupado@eco.cl").build();

            when(repository.findById(1L)).thenReturn(Optional.of(existente));
            when(repository.findByCorreo("ocupado@eco.cl"))
                    .thenReturn(Optional.of(PerfilUsuario.builder().id(99L).build()));

            assertThatThrownBy(() -> service.modificarDatosUsuario(1L, datosNuevos))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ocupado@eco.cl");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el usuario no existe")
        void lanzaExcepcionUsuarioInexistente() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.modificarDatosUsuario(999L, perfilBase))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("actualiza rol cuando datosNuevos trae rol no nulo")
        void actualizaRolSiSeProvee() {
            PerfilUsuario existente = PerfilUsuario.builder()
                    .id(1L).nombre("H").correo("h@eco.cl").rol(rolCliente).build();

            Rol rolAdmin = Rol.builder().id(2L).nombre("ADMIN").build();
            PerfilUsuario datosNuevos = PerfilUsuario.builder()
                    .nombre("H").correo("h@eco.cl").rol(rolAdmin).build();

            when(repository.findById(1L)).thenReturn(Optional.of(existente));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PerfilUsuario resultado = service.modificarDatosUsuario(1L, datosNuevos);

            assertThat(resultado.getRol().getNombre()).isEqualTo("ADMIN");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarUsuarios
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarUsuarios")
    class ListarUsuarios {

        @Test
        @DisplayName("retorna lista completa de usuarios")
        void retornaListaCompleta() {
            List<PerfilUsuario> lista = List.of(perfilBase,
                    PerfilUsuario.builder().id(2L).correo("b@eco.cl").nombre("B").build());

            when(repository.findAll()).thenReturn(lista);

            List<PerfilUsuario> resultado = service.listarUsuarios();

            assertThat(resultado).hasSize(2);
            verify(repository).findAll();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay usuarios")
        void retornaListaVacia() {
            when(repository.findAll()).thenReturn(List.of());

            assertThat(service.listarUsuarios()).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarPorRol
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarPorRol")
    class ListarPorRol {

        @Test
        @DisplayName("retorna solo usuarios con el rol especificado")
        void retornaUsuariosDelRol() {
            List<PerfilUsuario> clientes = List.of(perfilBase);
            when(repository.findByRol(rolCliente)).thenReturn(clientes);

            List<PerfilUsuario> resultado = service.listarPorRol(rolCliente);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getRol()).isEqualTo(rolCliente);
        }

        @Test
        @DisplayName("retorna lista vacía si no hay usuarios con ese rol")
        void retornaVacioSiNoHayUsuariosConEseRol() {
            Rol rolRaro = Rol.builder().id(99L).nombre("RARO").build();
            when(repository.findByRol(rolRaro)).thenReturn(List.of());

            assertThat(service.listarPorRol(rolRaro)).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarPorId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("retorna el usuario cuando existe")
        void retornaUsuarioCuandoExiste() {
            perfilBase.setId(1L);
            when(repository.findById(1L)).thenReturn(Optional.of(perfilBase));

            PerfilUsuario resultado = service.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getCorreo()).isEqualTo("hocx@eco.cl");
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el id no existe")
        void lanzaExcepcionCuandoNoExiste() {
            when(repository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(404L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("404");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarPorCorreo
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarPorCorreo")
    class BuscarPorCorreo {

        @Test
        @DisplayName("retorna el usuario cuando el correo existe")
        void retornaUsuarioCuandoExiste() {
            when(repository.findByCorreo("hocx@eco.cl")).thenReturn(Optional.of(perfilBase));

            PerfilUsuario resultado = service.buscarPorCorreo("hocx@eco.cl");

            assertThat(resultado.getCorreo()).isEqualTo("hocx@eco.cl");
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el correo no existe")
        void lanzaExcepcionCorreoNoExiste() {
            when(repository.findByCorreo("x@eco.cl")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorCorreo("x@eco.cl"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("x@eco.cl");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // configurarPermisos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("configurarPermisos")
    class ConfigurarPermisos {

        @Test
        @DisplayName("reemplaza permisos y retorna true")
        void configurarPermisosRetornaTrue() {
            PerfilUsuario usuario = PerfilUsuario.builder()
                    .id(1L).nombre("H").correo("h@eco.cl")
                    .permisos(new ArrayList<>())
                    .build();

            Permiso p1 = Permiso.builder().id(1L).nombre("LEER_PRODUCTOS").build();
            Permiso p2 = Permiso.builder().id(2L).nombre("EDITAR_USUARIOS").build();

            when(repository.findById(1L)).thenReturn(Optional.of(usuario));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Boolean resultado = service.configurarPermisos(1L, List.of(p1, p2));

            assertThat(resultado).isTrue();
            assertThat(usuario.getPermisos()).containsExactly(p1, p2);
        }

        @Test
        @DisplayName("limpia permisos anteriores antes de asignar los nuevos")
        void limpiaPermisosAntesDeAsignar() {
            Permiso viejo = Permiso.builder().id(99L).nombre("VIEJO").build();
            PerfilUsuario usuario = PerfilUsuario.builder()
                    .id(1L).nombre("H").correo("h@eco.cl")
                    .permisos(new ArrayList<>(List.of(viejo)))
                    .build();

            Permiso nuevo = Permiso.builder().id(1L).nombre("NUEVO").build();

            when(repository.findById(1L)).thenReturn(Optional.of(usuario));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.configurarPermisos(1L, List.of(nuevo));

            assertThat(usuario.getPermisos()).doesNotContain(viejo);
            assertThat(usuario.getPermisos()).containsOnly(nuevo);
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el usuario no existe")
        void lanzaExcepcionUsuarioNoExiste() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.configurarPermisos(999L, List.of()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // eliminarUsuario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("eliminarUsuario")
    class EliminarUsuario {

        @Test
        @DisplayName("elimina usuario existente y retorna true")
        void eliminaUsuarioYRetornaTrue() {
            perfilBase.setId(1L);
            when(repository.findById(1L)).thenReturn(Optional.of(perfilBase));
            doNothing().when(repository).delete(perfilBase);
            // restTemplate para log puede fallar sin romper el flujo
            when(restTemplate.postForEntity(contains("8084"), any(), eq(String.class)))
                    .thenReturn(null);

            Boolean resultado = service.eliminarUsuario(1L);

            assertThat(resultado).isTrue();
            verify(repository).delete(perfilBase);
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el usuario a eliminar no existe")
        void lanzaExcepcionAlEliminarInexistente() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.eliminarUsuario(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");

            verify(repository, never()).delete(any());
        }

        @Test
        @DisplayName("el fallo del log de analítica no interrumpe la eliminación")
        void falloLogNoRompeEliminacion() {
            perfilBase.setId(5L);
            when(repository.findById(5L)).thenReturn(Optional.of(perfilBase));
            doNothing().when(repository).delete(perfilBase);
            when(restTemplate.postForEntity(contains("8084"), any(), eq(String.class)))
                    .thenThrow(new RuntimeException("Analitica caída"));

            // No debe lanzar excepción — el catch interno silencia el error de log
            assertThatCode(() -> service.eliminarUsuario(5L)).doesNotThrowAnyException();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UsuarioService (clase separada)
    // ═════════════════════════════════════════════════════════════════════════

}