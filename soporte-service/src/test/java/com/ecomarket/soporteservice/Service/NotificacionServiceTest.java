package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.dto.ClienteDTO;
import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.model.entity.Notificacion;
import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.repository.NotificacionRepository;
import com.ecomarket.soporteservice.service.CanalNotificacionService;
import com.ecomarket.soporteservice.service.NotificacionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacionService")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repo;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CanalNotificacionService canalNotificacionService;

    @InjectMocks
    private NotificacionService service;

    @BeforeEach
    void setUp() {
        // inyecta el @Value sin levantar Spring
        ReflectionTestUtils.setField(service, "usuariosUrl", "http://localhost:8081");
    }

    private CanalNotificacion canal(Long id, String nombre) {
        CanalNotificacion c = new CanalNotificacion();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private Notificacion notif(Long id, Long destinatario, boolean enviada) {
        return Notificacion.builder()
                .id(id)
                .destinatarioId(destinatario)
                .canal(canal(1L, "EMAIL"))
                .titulo("Título")
                .cuerpo("Cuerpo")
                .enviadaConExito(enviada)
                .build();
    }

    @Nested
    @DisplayName("sendNotificacion")
    class SendNotificacion {

        @Test
        @DisplayName("envía con éxito cuando el canal y el destinatario existen")
        void enviaConExito() {
            when(canalNotificacionService.getCanalNotificacionById(1L)).thenReturn(canal(1L, "EMAIL"));
            when(restTemplate.getForObject(anyString(), eq(ClienteDTO.class))).thenReturn(new ClienteDTO());
            when(repo.save(any())).thenAnswer(inv -> {
                Notificacion n = inv.getArgument(0);
                n.setId(10L);
                return n;
            });

            Notificacion resultado = service.sendNotificacion(5L, "Pedido listo", "Tu pedido llegó", 1L);

            assertThat(resultado.getEnviadaConExito()).isTrue();
            assertThat(resultado.getDestinatarioId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("guarda con enviadaConExito=false cuando el destinatario no existe (404)")
        void marcaFalseCuandoDestinatarioNoExiste() {
            when(canalNotificacionService.getCanalNotificacionById(1L)).thenReturn(canal(1L, "EMAIL"));
            when(restTemplate.getForObject(anyString(), eq(ClienteDTO.class)))
                    .thenThrow(HttpClientErrorException.NotFound.class);
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Notificacion resultado = service.sendNotificacion(999L, "Hola", "Cuerpo", 1L);

            assertThat(resultado.getEnviadaConExito()).isFalse();
        }

        @Test
        @DisplayName("guarda con enviadaConExito=false cuando usuario-service no está disponible")
        void marcaFalseCuandoServicioNoDisponible() {
            when(canalNotificacionService.getCanalNotificacionById(1L)).thenReturn(canal(1L, "EMAIL"));
            when(restTemplate.getForObject(anyString(), eq(ClienteDTO.class)))
                    .thenThrow(new ResourceAccessException("Connection refused"));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Notificacion resultado = service.sendNotificacion(5L, "Hola", "Cuerpo", 1L);

            assertThat(resultado.getEnviadaConExito()).isFalse();
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el canal no existe")
        void lanzaExcepcionCanalInvalido() {
            when(canalNotificacionService.getCanalNotificacionById(99L))
                    .thenThrow(new NoExisteEnBdException("Canal 99 no existe."));

            assertThatThrownBy(() -> service.sendNotificacion(5L, "T", "C", 99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("guarda con enviadaConExito=false ante cualquier error inesperado")
        void marcaFalseAnteCualquierError() {
            when(canalNotificacionService.getCanalNotificacionById(1L)).thenReturn(canal(1L, "EMAIL"));
            when(restTemplate.getForObject(anyString(), eq(ClienteDTO.class)))
                    .thenThrow(new RuntimeException("Error inesperado"));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Notificacion resultado = service.sendNotificacion(5L, "T", "C", 1L);

            assertThat(resultado.getEnviadaConExito()).isFalse();
        }
    }

    @Nested
    @DisplayName("readAllNotificacion")
    class ReadAll {

        @Test
        @DisplayName("retorna lista completa de notificaciones")
        void retornaLista() {
            when(repo.findAll()).thenReturn(List.of(notif(1L, 5L, true), notif(2L, 6L, false)));
            assertThat(service.readAllNotificacion()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("readNotificacionesByDestinatarioId")
    class ByDestinatario {

        @Test
        @DisplayName("retorna notificaciones del destinatario")
        void retornaNotificaciones() {
            when(repo.findByDestinatarioId(5L)).thenReturn(List.of(notif(1L, 5L, true)));
            assertThat(service.readNotificacionesByDestinatarioId(5L)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findNotificacionById")
    class FindById {

        @Test
        @DisplayName("retorna la notificación cuando existe")
        void retornaNotificacion() {
            when(repo.findById(1L)).thenReturn(Optional.of(notif(1L, 5L, true)));
            assertThat(service.findNotificacionById(1L).getDestinatarioId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findNotificacionById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("deleteNotificacionById")
    class Delete {

        @Test
        @DisplayName("elimina notificación existente")
        void elimina() {
            when(repo.findById(1L)).thenReturn(Optional.of(notif(1L, 5L, true)));
            doNothing().when(repo).deleteById(1L);

            assertThatCode(() -> service.deleteNotificacionById(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteNotificacionById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).deleteById(any());
        }
    }
}