package com.ecomarket.soporteservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import com.ecomarket.soporteservice.dto.ClienteDTO;
import com.ecomarket.soporteservice.model.entity.Notificacion;
import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.repository.NotificacionRepository;
import com.ecomarket.soporteservice.service.CanalNotificacionService;
import com.ecomarket.soporteservice.service.NotificacionService;

public class NotificacionServiceTest {

    private static final Long NOTIFICACION_ID = 1L;
    private static final Long CANAL_NOTIFICACION_ID = 1L;
    private static final Long DESTINATARIO_ID = 1L;
    private static final String TITULO = "Titulo notificacion";
    private static final String MENSAJE = "Mensaje de la notificacion";

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private CanalNotificacionService canalNotificacionService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test

    void testReadAllNotificaciones() {
        CanalNotificacion canalMock = new CanalNotificacion();

        Notificacion notificacion = Notificacion.builder()
        .id(1L).destinatarioId(1L).canal(canalMock).titulo(TITULO).cuerpo(MENSAJE)
        .fechaEnvioNotificacion(LocalDateTime.now()).enviadaConExito(true).build();

        List<Notificacion> notificaciones = new ArrayList<>();
        notificaciones.add(notificacion);

        when(notificacionRepository.findAll()).thenReturn(notificaciones);

        List<Notificacion> resultado = notificacionService.readAllNotificacion();

        assertThat(resultado).hasSize(1);
        assertThat(resultado).contains(notificacion);

        verify(notificacionRepository).findAll();
    }

    @Test
    void testReadNotificacionesByDestinatarioId() {
        CanalNotificacion canalMock = new CanalNotificacion();

        Notificacion notificacion = Notificacion.builder()
        .id(1L).destinatarioId(DESTINATARIO_ID).canal(canalMock).titulo(TITULO).cuerpo(MENSAJE)
        .fechaEnvioNotificacion(LocalDateTime.now()).enviadaConExito(true).build();

        List<Notificacion> notificaciones = new ArrayList<>();
        notificaciones.add(notificacion);

        when(canalNotificacionService.getCanalNotificacionById(1L)).thenReturn(canalMock);
        when(notificacionRepository.findByDestinatarioId(DESTINATARIO_ID)).thenReturn(notificaciones);

        List<Notificacion> resultado = notificacionService.readNotificacionesByDestinatarioId(DESTINATARIO_ID);

        assertThat(resultado).hasSize(1);
        assertThat(resultado).contains(notificacion);

        verify(notificacionRepository).findByDestinatarioId(DESTINATARIO_ID);
    }

    @Test
    void testSendNotificacion() {

        CanalNotificacion canalMock = new CanalNotificacion();

        Notificacion notificacionGuardadaDB = Notificacion.builder()
        .id(1L).destinatarioId(1L).canal(canalMock).titulo(TITULO).cuerpo(MENSAJE)
        .fechaEnvioNotificacion(LocalDateTime.now()).enviadaConExito(true).build();

        when(canalNotificacionService.getCanalNotificacionById(1L)).thenReturn(canalMock);
        when(restTemplate.getForObject(any(String.class), eq(ClienteDTO.class))).thenReturn(new ClienteDTO());
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionGuardadaDB);

        Notificacion resultado = notificacionService.sendNotificacion(DESTINATARIO_ID, TITULO, MENSAJE, CANAL_NOTIFICACION_ID);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo(TITULO);
        assertThat(resultado.getEnviadaConExito()).isTrue();
        
        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    void testFindNotificacionById() {

        CanalNotificacion canalMock = new CanalNotificacion();
        
        Notificacion notificacionGuardadaDB = Notificacion.builder()
        .id(1L).destinatarioId(1L).canal(canalMock).titulo(TITULO).cuerpo(MENSAJE)
        .fechaEnvioNotificacion(LocalDateTime.now()).enviadaConExito(true).build();

        Optional<Notificacion> opt = Optional.ofNullable(notificacionGuardadaDB);

        when(notificacionRepository.findById(NOTIFICACION_ID)).thenReturn(opt);
        when(canalNotificacionService.getCanalNotificacionById(1L)).thenReturn(canalMock);

        Notificacion resultado = notificacionService.findNotificacionById(CANAL_NOTIFICACION_ID);
        
        assertThat(resultado).isEqualTo(notificacionGuardadaDB);

        verify(notificacionRepository).findById(NOTIFICACION_ID);
    }

}
