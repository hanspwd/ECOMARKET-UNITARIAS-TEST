package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.model.entity.MensajeChat;
import com.ecomarket.soporteservice.repository.MensajeChatRepository;
import com.ecomarket.soporteservice.service.MensajeChatService;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MensajeChatService")
class MensajeChatServiceTest {

    @Mock
    private MensajeChatRepository repo;

    @InjectMocks
    private MensajeChatService service;

    private MensajeChat mensaje(Long id, Long ticketId, String contenido, boolean leido) {
        MensajeChat m = new MensajeChat();
        m.setId(id);
        m.setTicketId(ticketId);
        m.setRemitenteId(1L);
        m.setEsCliente(true);
        m.setContenido(contenido);
        m.setFechaEnvio(LocalDateTime.now());
        m.setLeido(leido);
        return m;
    }

    @Nested
    @DisplayName("enviarMensajeChat")
    class EnviarMensaje {

        @Test
        @DisplayName("crea y retorna mensaje con leido=false y fecha asignada")
        void creaConLeidoFalse() {
            when(repo.save(any())).thenAnswer(inv -> {
                MensajeChat m = inv.getArgument(0);
                m.setId(1L);
                return m;
            });

            MensajeChat resultado = service.enviarMensajeChat(10L, 5L, true, "  Hola  ");

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getContenido()).isEqualTo("Hola"); // trim aplicado
            assertThat(resultado.getLeido()).isFalse();
            assertThat(resultado.getFechaEnvio()).isNotNull();
        }

        @Test
        @DisplayName("asigna correctamente ticketId y remitenteId")
        void asignaCamposCorrectos() {
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            MensajeChat resultado = service.enviarMensajeChat(99L, 7L, false, "Mensaje");

            assertThat(resultado.getTicketId()).isEqualTo(99L);
            assertThat(resultado.getRemitenteId()).isEqualTo(7L);
            assertThat(resultado.getEsCliente()).isFalse();
        }
    }

    @Nested
    @DisplayName("obtenerHistorialChat")
    class HistorialChat {

        @Test
        @DisplayName("retorna mensajes ordenados del ticket")
        void retornaHistorial() {
            List<MensajeChat> mensajes = List.of(
                    mensaje(1L, 10L, "Hola", false),
                    mensaje(2L, 10L, "¿En qué le ayudo?", false));

            when(repo.findByTicketIdOrderByFechaEnvioAsc(10L)).thenReturn(mensajes);

            List<MensajeChat> resultado = service.obtenerHistorialChat(10L);
            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("retorna lista vacía si el ticket no tiene mensajes")
        void retornaVacio() {
            when(repo.findByTicketIdOrderByFechaEnvioAsc(99L)).thenReturn(List.of());
            assertThat(service.obtenerHistorialChat(99L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("marcarComoLeido")
    class MarcarLeido {

        @Test
        @DisplayName("marca el mensaje como leído correctamente")
        void marcaLeido() {
            MensajeChat m = mensaje(1L, 10L, "Hola", false);
            when(repo.findById(1L)).thenReturn(Optional.of(m));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.marcarComoLeido(1L);

            assertThat(m.getLeido()).isTrue();
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el mensaje no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.marcarComoLeido(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("marcarTodosComoLeidos")
    class MarcarTodosLeidos {

        @Test
        @DisplayName("marca todos los mensajes del ticket como leídos")
        void marcaTodos() {
            List<MensajeChat> mensajes = List.of(
                    mensaje(1L, 10L, "Hola", false),
                    mensaje(2L, 10L, "Ok", false));

            when(repo.findByTicketIdOrderByFechaEnvioAsc(10L)).thenReturn(mensajes);
            when(repo.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            service.marcarTodosComoLeidos(10L);

            assertThat(mensajes).allMatch(MensajeChat::getLeido);
        }
    }

    @Nested
    @DisplayName("findMensajeById")
    class FindById {

        @Test
        @DisplayName("retorna el mensaje cuando existe")
        void retornaMensaje() {
            when(repo.findById(1L)).thenReturn(Optional.of(mensaje(1L, 10L, "Hola", false)));
            assertThat(service.findMensajeById(1L).getContenido()).isEqualTo("Hola");
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findMensajeById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("deleteMensajeById")
    class Delete {

        @Test
        @DisplayName("elimina mensaje existente sin excepción")
        void elimina() {
            when(repo.findById(1L)).thenReturn(Optional.of(mensaje(1L, 10L, "Hola", false)));
            doNothing().when(repo).deleteById(1L);

            assertThatCode(() -> service.deleteMensajeById(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteMensajeById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).deleteById(any());
        }
    }
}