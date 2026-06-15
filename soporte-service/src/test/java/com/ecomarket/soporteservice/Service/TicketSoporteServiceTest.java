package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.client.AnaliticaMetricaClient;
import com.ecomarket.soporteservice.dto.ClienteDTO;
import com.ecomarket.soporteservice.dto.PedidoDTO;
import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.exception.PedidoClienteIncompatibleException;
import com.ecomarket.soporteservice.model.entity.TicketSoporte;
import com.ecomarket.soporteservice.model.reference.CategoriaTicket;
import com.ecomarket.soporteservice.model.reference.EstadoTicket;
import com.ecomarket.soporteservice.repository.TicketSoporteRepository;
import com.ecomarket.soporteservice.service.CategoriaTicketService;
import com.ecomarket.soporteservice.service.EstadoTicketService;
import com.ecomarket.soporteservice.service.TicketSoporteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketSoporteService")
class TicketSoporteServiceTest {

    @Mock private TicketSoporteRepository repo;
    @Mock private EstadoTicketService estadoTicketService;
    @Mock private CategoriaTicketService categoriaTicketService;
    @Mock private RestTemplate restTemplate;
    @Mock private AnaliticaMetricaClient analiticaMetricaClient;

    @InjectMocks
    private TicketSoporteService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "usuariosUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(service, "pedidosUrl",  "http://localhost:8082");
    }

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private EstadoTicket estado(Long id, String nombre) {
        EstadoTicket e = new EstadoTicket();
        e.setId(id);
        e.setNombre(nombre);
        return e;
    }

    private CategoriaTicket categoria(Long id, String nombre) {
        CategoriaTicket c = new CategoriaTicket();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private TicketSoporte ticket(Long id, Long clienteId) {
        TicketSoporte t = new TicketSoporte();
        t.setId(id);
        t.setClienteId(clienteId);
        t.setAsunto("Problema con entrega");
        t.setEstado(estado(1L, "ABIERTO"));
        t.setCategoria(categoria(1L, "ENTREGA"));
        t.setFechaCreacion(LocalDateTime.now());
        t.setPedidoRelacionadoId(10L);
        return t;
    }

    private PedidoDTO pedido(Long id, Long clienteId) {
        PedidoDTO p = new PedidoDTO();
        p.setId(id);
        p.setClienteId(clienteId);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // readAllTickets
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("readAllTickets")
    class ReadAll {

        @Test
        @DisplayName("retorna lista completa de tickets")
        void retornaLista() {
            when(repo.findAll()).thenReturn(List.of(ticket(1L, 5L), ticket(2L, 6L)));
            assertThat(service.readAllTickets()).hasSize(2);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay tickets")
        void retornaVacio() {
            when(repo.findAll()).thenReturn(List.of());
            assertThat(service.readAllTickets()).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // readTicketsByClienteId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("readTicketsByClienteId")
    class ByCliente {

        @Test
        @DisplayName("retorna tickets del cliente")
        void retornaTicketsCliente() {
            when(repo.findByClienteId(5L)).thenReturn(List.of(ticket(1L, 5L)));
            assertThat(service.readTicketsByClienteId(5L)).hasSize(1);
        }

        @Test
        @DisplayName("retorna vacío si el cliente no tiene tickets")
        void retornaVacio() {
            when(repo.findByClienteId(99L)).thenReturn(List.of());
            assertThat(service.readTicketsByClienteId(99L)).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // readTicketsByEstadoId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("readTicketsByEstadoId")
    class ByEstado {

        @Test
        @DisplayName("retorna tickets con el estado dado")
        void retornaTicketsPorEstado() {
            when(repo.findByEstadoId(1L)).thenReturn(List.of(ticket(1L, 5L), ticket(2L, 6L)));
            assertThat(service.readTicketsByEstadoId(1L)).hasSize(2);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // findTicketById
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findTicketById")
    class FindById {

        @Test
        @DisplayName("retorna el ticket cuando existe")
        void retornaTicket() {
            when(repo.findById(1L)).thenReturn(Optional.of(ticket(1L, 5L)));
            assertThat(service.findTicketById(1L).getClienteId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findTicketById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ingresarTicket
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ingresarTicket")
    class IngresarTicket {

        @Test
        @DisplayName("crea ticket correctamente cuando cliente y pedido son válidos y compatibles")
        void creaTicketExitoso() throws Exception {
            when(estadoTicketService.findEstadoTicketById(1L)).thenReturn(estado(1L, "ABIERTO"));
            when(categoriaTicketService.findCategoriaTicketById(1L)).thenReturn(categoria(1L, "ENTREGA"));
            when(restTemplate.getForObject(contains("/api/usuarios/5"), eq(ClienteDTO.class)))
                    .thenReturn(new ClienteDTO());
            when(restTemplate.getForObject(contains("/api/pedido/10"), eq(PedidoDTO.class)))
                    .thenReturn(pedido(10L, 5L));
            when(repo.save(any())).thenAnswer(inv -> {
                TicketSoporte t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });

            TicketSoporte resultado = service.ingresarTicket(5L, 1L, "  Problema entrega  ", 10L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getAsunto()).isEqualTo("Problema entrega"); // trim
            assertThat(resultado.getClienteId()).isEqualTo(5L);
            assertThat(resultado.getFechaCreacion()).isNotNull();
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el cliente no existe (404)")
        void lanzaExcepcionClienteNoExiste() {
            when(estadoTicketService.findEstadoTicketById(1L)).thenReturn(estado(1L, "ABIERTO"));
            when(categoriaTicketService.findCategoriaTicketById(1L)).thenReturn(categoria(1L, "ENTREGA"));
            when(restTemplate.getForObject(contains("/api/usuarios/"), eq(ClienteDTO.class)))
                    .thenThrow(HttpClientErrorException.NotFound.class);

            assertThatThrownBy(() -> service.ingresarTicket(999L, 1L, "Asunto", 10L))
                    .isInstanceOf(NoExisteEnBdException.class);

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando usuario-service no está disponible")
        void lanzaExcepcionServicioUsuariosNoDisponible() {
            when(estadoTicketService.findEstadoTicketById(1L)).thenReturn(estado(1L, "ABIERTO"));
            when(categoriaTicketService.findCategoriaTicketById(1L)).thenReturn(categoria(1L, "ENTREGA"));
            when(restTemplate.getForObject(contains("/api/usuarios/"), eq(ClienteDTO.class)))
                    .thenThrow(new ResourceAccessException("Connection refused"));

            assertThatThrownBy(() -> service.ingresarTicket(5L, 1L, "Asunto", 10L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("usuarios");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el pedido no existe (404)")
        void lanzaExcepcionPedidoNoExiste() {
            when(estadoTicketService.findEstadoTicketById(1L)).thenReturn(estado(1L, "ABIERTO"));
            when(categoriaTicketService.findCategoriaTicketById(1L)).thenReturn(categoria(1L, "ENTREGA"));
            when(restTemplate.getForObject(contains("/api/usuarios/"), eq(ClienteDTO.class)))
                    .thenReturn(new ClienteDTO());
            when(restTemplate.getForObject(contains("/api/pedido/"), eq(PedidoDTO.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

            assertThatThrownBy(() -> service.ingresarTicket(5L, 1L, "Asunto", 99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("pedido");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando pedidos-service no está disponible")
        void lanzaExcepcionServicioPedidosNoDisponible() {
            when(estadoTicketService.findEstadoTicketById(1L)).thenReturn(estado(1L, "ABIERTO"));
            when(categoriaTicketService.findCategoriaTicketById(1L)).thenReturn(categoria(1L, "ENTREGA"));
            when(restTemplate.getForObject(contains("/api/usuarios/"), eq(ClienteDTO.class)))
                    .thenReturn(new ClienteDTO());
            when(restTemplate.getForObject(contains("/api/pedido/"), eq(PedidoDTO.class)))
                    .thenThrow(new ResourceAccessException("Connection refused"));

            assertThatThrownBy(() -> service.ingresarTicket(5L, 1L, "Asunto", 10L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("pedidos");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("lanza PedidoClienteIncompatibleException cuando pedido pertenece a otro cliente")
        void lanzaExcepcionPedidoIncompatible() {
            when(estadoTicketService.findEstadoTicketById(1L)).thenReturn(estado(1L, "ABIERTO"));
            when(categoriaTicketService.findCategoriaTicketById(1L)).thenReturn(categoria(1L, "ENTREGA"));
            when(restTemplate.getForObject(contains("/api/usuarios/"), eq(ClienteDTO.class)))
                    .thenReturn(new ClienteDTO());
            // pedido pertenece al cliente 99, no al 5
            when(restTemplate.getForObject(contains("/api/pedido/"), eq(PedidoDTO.class)))
                    .thenReturn(pedido(10L, 99L));

            assertThatThrownBy(() -> service.ingresarTicket(5L, 1L, "Asunto", 10L))
                    .isInstanceOf(PedidoClienteIncompatibleException.class);

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("lanza Exception genérica ante error HTTP inesperado en pedidos-service")
        void lanzaExcepcionHttpInesperada() {
            when(estadoTicketService.findEstadoTicketById(1L)).thenReturn(estado(1L, "ABIERTO"));
            when(categoriaTicketService.findCategoriaTicketById(1L)).thenReturn(categoria(1L, "ENTREGA"));
            when(restTemplate.getForObject(contains("/api/usuarios/"), eq(ClienteDTO.class)))
                    .thenReturn(new ClienteDTO());
            when(restTemplate.getForObject(contains("/api/pedido/"), eq(PedidoDTO.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            assertThatThrownBy(() -> service.ingresarTicket(5L, 1L, "Asunto", 10L))
                    .isInstanceOf(Exception.class);

            verify(repo, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // actualizarEstadoTicket
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("actualizarEstadoTicket")
    class ActualizarEstado {

        @Test
        @DisplayName("actualiza el estado sin asignar fechaCierre para estados intermedios")
        void actualizaEstadoIntermedio() {
            TicketSoporte t = ticket(1L, 5L);
            when(repo.findById(1L)).thenReturn(Optional.of(t));
            when(estadoTicketService.findEstadoTicketById(2L)).thenReturn(estado(2L, "EN_PROCESO"));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TicketSoporte resultado = service.actualizarEstadoTicket(1L, 2L);

            assertThat(resultado.getEstado().getNombre()).isEqualTo("EN_PROCESO");
            assertThat(resultado.getFechaCierre()).isNull();
        }

        @Test
        @DisplayName("asigna fechaCierre cuando el nuevo estado es 4 (RESUELTO)")
        void asignaFechaCierreEstado4() {
            TicketSoporte t = ticket(1L, 5L);
            when(repo.findById(1L)).thenReturn(Optional.of(t));
            when(estadoTicketService.findEstadoTicketById(4L)).thenReturn(estado(4L, "RESUELTO"));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TicketSoporte resultado = service.actualizarEstadoTicket(1L, 4L);

            assertThat(resultado.getFechaCierre()).isNotNull();
        }

        @Test
        @DisplayName("asigna fechaCierre cuando el nuevo estado es 5 (CERRADO)")
        void asignaFechaCierreEstado5() {
            TicketSoporte t = ticket(1L, 5L);
            when(repo.findById(1L)).thenReturn(Optional.of(t));
            when(estadoTicketService.findEstadoTicketById(5L)).thenReturn(estado(5L, "CERRADO"));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TicketSoporte resultado = service.actualizarEstadoTicket(1L, 5L);

            assertThat(resultado.getFechaCierre()).isNotNull();
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el ticket no existe")
        void lanzaExcepcionTicketNoExiste() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.actualizarEstadoTicket(99L, 2L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // asignarTicketEmpleado
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("asignarTicketEmpleado")
    class AsignarEmpleado {

        @Test
        @DisplayName("asigna el empleado al ticket correctamente")
        void asignaEmpleado() {
            TicketSoporte t = ticket(1L, 5L);
            when(repo.findById(1L)).thenReturn(Optional.of(t));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TicketSoporte resultado = service.asignarTicketEmpleado(1L, 7L);

            assertThat(resultado.getEmpleadoAsignadoId()).isEqualTo(7L);
            verify(repo).save(t);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el ticket no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.asignarTicketEmpleado(99L, 7L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // solucionarTicket
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("solucionarTicket")
    class SolucionarTicket {

        @Test
        @DisplayName("resuelve ticket: estado RESUELTO, solución y fechaCierre asignados")
        void solucionaTicket() {
            TicketSoporte t = ticket(1L, 5L);
            when(repo.findById(1L)).thenReturn(Optional.of(t));
            when(estadoTicketService.findEstadoTicketById(4L)).thenReturn(estado(4L, "RESUELTO"));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(analiticaMetricaClient).registrarMetrica(anyString(), anyDouble(), anyString());

            TicketSoporte resultado = service.solucionarTicket(1L, "Se reenvió el paquete.");

            assertThat(resultado.getEstado().getNombre()).isEqualTo("RESUELTO");
            assertThat(resultado.getSolucionResumen()).isEqualTo("Se reenvió el paquete.");
            assertThat(resultado.getFechaCierre()).isNotNull();
            verify(analiticaMetricaClient).registrarMetrica(eq("soporte.tickets.resueltos"), eq(1.0), anyString());
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el ticket no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.solucionarTicket(99L, "Solución"))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // deleteTicketById
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteTicketById")
    class Delete {

        @Test
        @DisplayName("elimina ticket existente sin lanzar excepción")
        void eliminaTicket() {
            when(repo.findById(1L)).thenReturn(Optional.of(ticket(1L, 5L)));
            doNothing().when(repo).deleteById(1L);

            assertThatCode(() -> service.deleteTicketById(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el ticket no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteTicketById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).deleteById(any());
        }
    }
}