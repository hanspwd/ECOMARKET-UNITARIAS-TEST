package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.model.entity.MensajeChat;
import com.ecomarket.soporteservice.model.entity.Notificacion;
import com.ecomarket.soporteservice.model.entity.Resena;
import com.ecomarket.soporteservice.model.entity.TicketSoporte;
import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.model.reference.CategoriaTicket;
import com.ecomarket.soporteservice.model.reference.EstadoTicket;
import com.ecomarket.soporteservice.service.MensajeChatService;
import com.ecomarket.soporteservice.service.NotificacionService;
import com.ecomarket.soporteservice.service.ResenaService;
import com.ecomarket.soporteservice.service.SoporteService;
import com.ecomarket.soporteservice.service.TicketSoporteService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para SoporteService (facade).
 * Verifica que cada método delega correctamente al service correspondiente.
 *
 * Ejecutar:
 *   mvn test -pl soporte-service -Dtest=SoporteServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SoporteService")
class SoporteServiceTest {

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private TicketSoporteService ticketSoporteService;

    @Mock
    private MensajeChatService mensajeChatService;

    @Mock
    private ResenaService resenaService;

    @InjectMocks
    private SoporteService service;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Notificacion notif() {
        return Notificacion.builder()
                .id(1L).destinatarioId(5L)
                .canal(new CanalNotificacion(1L, "EMAIL"))
                .titulo("Título").cuerpo("Cuerpo")
                .fechaEnvioNotificacion(LocalDateTime.now())
                .enviadaConExito(true)
                .build();
    }

    private TicketSoporte ticket() {
        TicketSoporte t = new TicketSoporte();
        t.setId(1L);
        t.setClienteId(5L);
        t.setAsunto("Problema con entrega");
        EstadoTicket estado = new EstadoTicket(); estado.setId(1L); estado.setNombre("ABIERTO");
        t.setEstado(estado);
        CategoriaTicket cat = new CategoriaTicket(); cat.setId(1L); cat.setNombre("ENTREGA");
        t.setCategoria(cat);
        t.setFechaCreacion(LocalDateTime.now());
        t.setPedidoRelacionadoId(10L);
        return t;
    }

    private MensajeChat mensaje() {
        MensajeChat m = new MensajeChat();
        m.setId(1L); m.setTicketId(1L); m.setRemitenteId(5L);
        m.setEsCliente(true); m.setContenido("Hola");
        m.setFechaEnvio(LocalDateTime.now()); m.setLeido(false);
        return m;
    }

    private Resena resena() {
        Resena r = new Resena();
        r.setId(1L); r.setProductoId(10L); r.setClienteId(5L);
        r.setCalificacionEstrellas(8); r.setComentario("Muy buen producto");
        r.setFechaPublicacion(LocalDateTime.now()); r.setModeracionAprobado(false);
        return r;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // enviarNotificacionPush
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("enviarNotificacionPush")
    class EnviarNotificacion {

        @Test
        @DisplayName("delega a notificacionService.sendNotificacion y retorna resultado")
        void delegaCorrectamente() {
            when(notificacionService.sendNotificacion(5L, "Pedido listo", "Tu pedido llegó", 1L))
                    .thenReturn(notif());

            Notificacion resultado = service.enviarNotificacionPush(5L, "Pedido listo", "Tu pedido llegó", 1L);

            assertThat(resultado.getDestinatarioId()).isEqualTo(5L);
            assertThat(resultado.getEnviadaConExito()).isTrue();
            verify(notificacionService).sendNotificacion(5L, "Pedido listo", "Tu pedido llegó", 1L);
        }

        @Test
        @DisplayName("propaga la excepción si el canal no existe")
        void propagaExcepcion() {
            when(notificacionService.sendNotificacion(anyLong(), anyString(), anyString(), eq(99L)))
                    .thenThrow(new RuntimeException("Canal 99 no existe."));

            assertThatThrownBy(() -> service.enviarNotificacionPush(5L, "T", "C", 99L))
                    .isInstanceOf(RuntimeException.class)
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
        @DisplayName("delega a ticketSoporteService.ingresarTicket y retorna el ticket creado")
        void delegaCorrectamente() throws Exception {
            when(ticketSoporteService.ingresarTicket(5L, 1L, "Problema entrega", 10L))
                    .thenReturn(ticket());

            TicketSoporte resultado = service.ingresarTicket(5L, 1L, "Problema entrega", 10L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getClienteId()).isEqualTo(5L);
            verify(ticketSoporteService).ingresarTicket(5L, 1L, "Problema entrega", 10L);
        }

        @Test
        @DisplayName("propaga la excepción si el cliente no existe")
        void propagaExcepcion() throws Exception {
            when(ticketSoporteService.ingresarTicket(anyLong(), anyLong(), anyString(), anyLong()))
                    .thenThrow(new RuntimeException("Cliente no encontrado."));

            assertThatThrownBy(() -> service.ingresarTicket(999L, 1L, "Asunto", 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cliente");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // asignarTicketEmpleado
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("asignarTicketEmpleado")
    class AsignarEmpleado {

        @Test
        @DisplayName("delega y retorna el ticket con empleado asignado")
        void delegaCorrectamente() {
            TicketSoporte t = ticket();
            t.setEmpleadoAsignadoId(7L);
            when(ticketSoporteService.asignarTicketEmpleado(1L, 7L)).thenReturn(t);

            TicketSoporte resultado = service.asignarTicketEmpleado(1L, 7L);

            assertThat(resultado.getEmpleadoAsignadoId()).isEqualTo(7L);
            verify(ticketSoporteService).asignarTicketEmpleado(1L, 7L);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // enviarMensajeChat
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("enviarMensajeChat")
    class EnviarMensaje {

        @Test
        @DisplayName("delega y retorna el mensaje enviado")
        void delegaCorrectamente() {
            when(mensajeChatService.enviarMensajeChat(1L, 5L, true, "Hola")).thenReturn(mensaje());

            MensajeChat resultado = service.enviarMensajeChat(1L, 5L, true, "Hola");

            assertThat(resultado.getContenido()).isEqualTo("Hola");
            assertThat(resultado.getLeido()).isFalse();
            verify(mensajeChatService).enviarMensajeChat(1L, 5L, true, "Hola");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // obtenerHistorialChat
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("obtenerHistorialChat")
    class HistorialChat {

        @Test
        @DisplayName("delega y retorna el historial del ticket")
        void delegaCorrectamente() {
            when(mensajeChatService.obtenerHistorialChat(1L)).thenReturn(List.of(mensaje()));

            List<MensajeChat> resultado = service.obtenerHistorialChat(1L);

            assertThat(resultado).hasSize(1);
            verify(mensajeChatService).obtenerHistorialChat(1L);
        }

        @Test
        @DisplayName("retorna lista vacía si no hay mensajes")
        void retornaVacio() {
            when(mensajeChatService.obtenerHistorialChat(99L)).thenReturn(List.of());
            assertThat(service.obtenerHistorialChat(99L)).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // solucionarTicket
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("solucionarTicket")
    class SolucionarTicket {

        @Test
        @DisplayName("delega y retorna el ticket con solución y estado RESUELTO")
        void delegaCorrectamente() {
            TicketSoporte t = ticket();
            EstadoTicket resuelto = new EstadoTicket(); resuelto.setId(4L); resuelto.setNombre("RESUELTO");
            t.setEstado(resuelto);
            t.setSolucionResumen("Se reenvió el paquete.");
            t.setFechaCierre(LocalDateTime.now());

            when(ticketSoporteService.solucionarTicket(1L, "Se reenvió el paquete.")).thenReturn(t);

            TicketSoporte resultado = service.solucionarTicket(1L, "Se reenvió el paquete.");

            assertThat(resultado.getSolucionResumen()).isEqualTo("Se reenvió el paquete.");
            assertThat(resultado.getEstado().getNombre()).isEqualTo("RESUELTO");
            assertThat(resultado.getFechaCierre()).isNotNull();
            verify(ticketSoporteService).solucionarTicket(1L, "Se reenvió el paquete.");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // dejarResena
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("dejarResena")
    class DejarResena {

        @Test
        @DisplayName("delega y retorna la reseña creada con moderacionAprobado=false")
        void delegaCorrectamente() {
            when(resenaService.dejarResena(10L, 5L, 8, "Muy buen producto")).thenReturn(resena());

            Resena resultado = service.dejarResena(10L, 5L, 8, "Muy buen producto");

            assertThat(resultado.getModeracionAprobado()).isFalse();
            assertThat(resultado.getCalificacionEstrellas()).isEqualTo(8);
            verify(resenaService).dejarResena(10L, 5L, 8, "Muy buen producto");
        }
    }
}