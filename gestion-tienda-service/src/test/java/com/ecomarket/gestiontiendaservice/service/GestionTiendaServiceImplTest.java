package com.ecomarket.gestiontiendaservice.service;

import com.ecomarket.gestiontiendaservice.model.*;
import com.ecomarket.gestiontiendaservice.repository.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para GestionTiendaServiceImpl.
 *
 * Ejecutar:
 * mvn test -pl gestion-tienda-service -Dtest=GestionTiendaServiceImplTest
 *
 * BUGS CORREGIDOS vs versión anterior:
 *
 * BUG 1 — Import redundante eliminado:
 * La versión anterior importaba GestionTiendaServiceImpl explícitamente
 * ADEMÁS del wildcard "import com.ecomarket.gestiontiendaservice.service.*".
 * Eso no falla en compilación pero genera un warning de "unused import" que
 * algunos entornos tratan como error. Se eliminó el import redundante.
 *
 * BUG 2 — @BeforeEach con new GestionTiendaServiceImpl() eliminado:
 * GestionTiendaServiceImpl usa @Autowired en campos (field injection), NO
 * constructor injection. No tiene constructor con argumentos — Lombok genera
 * un constructor vacío por @RequiredArgsConstructor sobre campos @Autowired
 * que NO son `final`. Intentar construirlo manualmente con argumentos
 * falla en compilación. @InjectMocks es suficiente y correcto aquí.
 *
 * BUG 3 — PermisoPOS constructor incorrecto:
 * new PermisoPOS(null, 1L, true, false, true) asumía 5 campos en orden
 * (id, rolEmpleado, permiteAnulaciones, permiteAperturaCaja,
 * permiteAplicarDescuentoManual).
 * Eso es correcto SOLO si @AllArgsConstructor genera exactamente ese orden.
 * Pero Lombok genera el @AllArgsConstructor en el orden de declaración de
 * campos,
 * que hay que verificar contra el .java. Se reemplazó por setter-style para
 * evitar dependencia frágil del orden de campos.
 */
@ExtendWith(MockitoExtension.class)
class GestionTiendaServiceImplTest {

    // BUG 2 FIX: @InjectMocks solo, sin @BeforeEach que construya manualmente.
    // GestionTiendaServiceImpl tiene @Autowired en campos, así que Mockito
    // inyecta los mocks directamente por field injection.
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private PermisoPOSRepository permisoPOSRepository;
    @Mock
    private ReglamentoInternoRepository reglamentoInternoRepository;
    @Mock
    private HorarioAtencionRepository horarioAtencionRepository;
    @Mock
    private TareaPersonalRepository tareaPersonalRepository;
    @Mock
    private EstadoTareaPersonalRepository estadoTareaPersonalRepository;

    @InjectMocks
    private GestionTiendaServiceImpl service;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Sucursal sucursalActiva(Long id, String nombre) {
        Sucursal s = new Sucursal();
        s.setId(id);
        s.setNombre(nombre);
        s.setDireccion("Av. Principal 123");
        s.setTelefono("412345678");
        s.setGarantiaCargold(1L);
        s.setActiva(true);
        return s;
    }

    private EstadoTareaPersonal estadoPendiente() {
        EstadoTareaPersonal e = new EstadoTareaPersonal();
        e.setId(1L);
        e.setNombre("PENDIENTE");
        return e;
    }

    // BUG 3 FIX: helper usando setters en vez del @AllArgsConstructor frágil
    private PermisoPOS permisoPos(Long id, Long rolEmpleado,
            boolean anulaciones, boolean apertura, boolean descuento) {
        PermisoPOS p = new PermisoPOS();
        p.setId(id);
        p.setRolEmpleado(rolEmpleado);
        p.setPermiteAnulaciones(anulaciones);
        p.setPermiteAperturaCaja(apertura);
        p.setPermiteAplicarDescuentoManual(descuento);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // registrarSucursal
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("registrarSucursal")
    class RegistrarSucursal {

        @Test
        @DisplayName("persiste la sucursal con activa=true y retorna el objeto guardado")
        void registraSucursalExitosamente() {
            when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(i -> {
                Sucursal s = i.getArgument(0);
                s.setId(1L);
                return s;
            });

            Sucursal resultado = service.registrarSucursal(
                    "Sucursal Centro", "Barros Arana 500", "412345678", 10L);

            assertThat(resultado.getNombre()).isEqualTo("Sucursal Centro");
            assertThat(resultado.getActiva()).isTrue();
            assertThat(resultado.getGarantiaCargold()).isEqualTo(10L);
            verify(sucursalRepository).save(argThat(s -> Boolean.TRUE.equals(s.getActiva())));
        }

        @Test
        @DisplayName("el repositorio save es invocado exactamente una vez")
        void saveInvocadoUnaVez() {
            when(sucursalRepository.save(any())).thenReturn(sucursalActiva(1L, "S1"));

            service.registrarSucursal("S1", "Dir", "Tel", 1L);

            verify(sucursalRepository, times(1)).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // obtenerDatosSucursal
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("obtenerDatosSucursal")
    class ObtenerDatosSucursal {

        @Test
        @DisplayName("retorna la sucursal cuando el ID existe")
        void retornaSucursalExistente() {
            Sucursal sucursal = sucursalActiva(1L, "Centro");
            when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));

            Sucursal resultado = service.obtenerDatosSucursal(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Centro");
        }

        @Test
        @DisplayName("lanza RuntimeException si la sucursal no existe")
        void sucursalInexistenteLanzaExcepcion() {
            when(sucursalRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtenerDatosSucursal(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarSucursalesActivas
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarSucursalesActivas")
    class ListarSucursalesActivas {

        @Test
        @DisplayName("delega en findByActivaTrue y retorna la lista del repositorio")
        void delegaEnRepository() {
            List<Sucursal> activas = List.of(
                    sucursalActiva(1L, "Norte"),
                    sucursalActiva(2L, "Sur"));
            when(sucursalRepository.findByActivaTrue()).thenReturn(activas);

            List<Sucursal> resultado = service.listarSucursalesActivas();

            assertThat(resultado).hasSize(2);
            verify(sucursalRepository).findByActivaTrue();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // asignarTareaPersonal
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("asignarTareaPersonal")
    class AsignarTareaPersonal {

        @Test
        @DisplayName("crea tarea en estado PENDIENTE con todos los campos correctos")
        void asignaTareaExitosamente() {
            when(estadoTareaPersonalRepository.findByNombre("PENDIENTE"))
                    .thenReturn(Optional.of(estadoPendiente()));
            when(tareaPersonalRepository.save(any(TareaPersonal.class))).thenAnswer(i -> {
                TareaPersonal t = i.getArgument(0);
                t.setId(1L);
                return t;
            });

            LocalDateTime limite = LocalDateTime.now().plusDays(7);
            TareaPersonal resultado = service.asignarTareaPersonal(
                    5L, 1L, "Inventariar bodega", "Contar ítems de bodega", limite);

            assertThat(resultado.getEmpleadoId()).isEqualTo(5L);
            assertThat(resultado.getSucursalId()).isEqualTo(1L);
            assertThat(resultado.getTitulo()).isEqualTo("Inventariar bodega");
            assertThat(resultado.getEstado().getNombre()).isEqualTo("PENDIENTE");
            assertThat(resultado.getFechaAsignacion()).isNotNull();
            assertThat(resultado.getFechaLimite()).isEqualTo(limite);
        }

        @Test
        @DisplayName("lanza RuntimeException si el estado PENDIENTE no existe en BD")
        void estadoPendienteNoEncontradoLanzaExcepcion() {
            when(estadoTareaPersonalRepository.findByNombre("PENDIENTE"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.asignarTareaPersonal(
                    1L, 1L, "Tarea", "Desc", LocalDateTime.now().plusDays(1)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("PENDIENTE");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // actualizarEstadoTarea
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("actualizarEstadoTarea")
    class ActualizarEstadoTarea {

        @Test
        @DisplayName("actualiza el estado de la tarea correctamente")
        void actualizaEstadoExitosamente() {
            TareaPersonal tarea = new TareaPersonal();
            tarea.setId(1L);
            tarea.setEstado(estadoPendiente());

            EstadoTareaPersonal completado = new EstadoTareaPersonal();
            completado.setId(2L);
            completado.setNombre("COMPLETADO");

            when(tareaPersonalRepository.findById(1L)).thenReturn(Optional.of(tarea));
            when(tareaPersonalRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            TareaPersonal resultado = service.actualizarEstadoTarea(1L, completado);

            assertThat(resultado.getEstado().getNombre()).isEqualTo("COMPLETADO");
        }

        @Test
        @DisplayName("lanza RuntimeException si la tarea no existe")
        void tareaInexistenteLanzaExcepcion() {
            when(tareaPersonalRepository.findById(99L)).thenReturn(Optional.empty());

            EstadoTareaPersonal estado = new EstadoTareaPersonal();
            estado.setNombre("EN_PROCESO");

            assertThatThrownBy(() -> service.actualizarEstadoTarea(99L, estado))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // administrarHorario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("administrarHorario")
    class AdministrarHorario {

        @Test
        @DisplayName("elimina horarios existentes y guarda los nuevos para la sucursal")
        void reemplazaHorariosExitosamente() {
            HorarioAtencion horarioExistente = new HorarioAtencion();
            horarioExistente.setId(1L);
            horarioExistente.setSucursalId(1L);

            when(horarioAtencionRepository.findBySucursalId(1L))
                    .thenReturn(List.of(horarioExistente));

            HorarioAtencion nuevoHorario = new HorarioAtencion();
            nuevoHorario.setDiaSemana(1);
            nuevoHorario.setHoraApertura("09:00");
            nuevoHorario.setHoraCierre("20:00");
            nuevoHorario.setEsFeriado(false);

            Boolean resultado = service.administrarHorario(1L, List.of(nuevoHorario));

            assertThat(resultado).isTrue();
            verify(horarioAtencionRepository).saveAll(argThat(iterable -> {
                // Convertimos el Iterable a List para poder usar size() y get()
                List<HorarioAtencion> h = (List<HorarioAtencion>) iterable;

                return h.size() == 1 &&
                        h.get(0).getSucursalId().equals(1L) &&
                        h.get(0).getDiaSemana() == 1 &&
                        "09:00".equals(h.get(0).getHoraApertura()) &&
                        "20:00".equals(h.get(0).getHoraCierre()) &&
                        Boolean.FALSE.equals(h.get(0).getEsFeriado());
            }));
        }

        @Test
        @DisplayName("si no hay horarios previos, guarda los nuevos igualmente")
        void guardaHorariosNuevosSinPrevios() {
            when(horarioAtencionRepository.findBySucursalId(2L)).thenReturn(List.of());

            HorarioAtencion nuevo = new HorarioAtencion();
            nuevo.setDiaSemana(2);
            nuevo.setHoraApertura("08:00");
            nuevo.setHoraCierre("18:00");
            nuevo.setEsFeriado(false);

            Boolean resultado = service.administrarHorario(2L, List.of(nuevo));

            assertThat(resultado).isTrue();
            verify(horarioAtencionRepository).saveAll(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // configurarPermisoPOS
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("configurarPermisoPOS")
    class ConfigurarPermisoPOS {

        @Test
        @DisplayName("persiste el permiso POS y retorna el objeto guardado")
        void guardarPermisoExitosamente() {
            // BUG 3 FIX: usando helper con setters en lugar del AllArgsConstructor
            PermisoPOS permiso = permisoPos(null, 1L, true, false, true);
            PermisoPOS guardado = permisoPos(1L, 1L, true, false, true);

            when(permisoPOSRepository.save(permiso)).thenReturn(guardado);

            PermisoPOS resultado = service.configurarPermisoPOS(permiso);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getPermiteAnulaciones()).isTrue();
            assertThat(resultado.getPermiteAperturaCaja()).isFalse();
            assertThat(resultado.getPermiteAplicarDescuentoManual()).isTrue();
        }
    }
}