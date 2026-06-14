package com.ecomarket.soporteservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.ecomarket.soporteservice.dto.ClienteDTO;
import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.model.entity.Notificacion;
import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.repository.NotificacionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class NotificacionService {
    
    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CanalNotificacionService canalNotificacionService;

    @org.springframework.beans.factory.annotation.Value("${microservicio.usuarios.url}")
    private String usuariosUrl;

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public List<Notificacion> readAllNotificacion() {
        return notificacionRepository.findAll();
    }

    public List<Notificacion> readNotificacionesByDestinatarioId(Long destinatarioId) {
        return notificacionRepository.findByDestinatarioId(destinatarioId);
    }

    public Notificacion findNotificacionById(Long id) {
        return notificacionRepository.findById(id)
            .orElseThrow(() -> new NoExisteEnBdException("La notificacion con id " + id + " no existe en la DB."));
    }

    public void deleteNotificacionById(Long id) {
        Notificacion existente = notificacionRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("La notificacion con id " + id + " no se puede borrar debido a que no existe en la BD.");
        }
        notificacionRepository.deleteById(id);
    }

    public Notificacion sendNotificacion(Long destinatarioId, String titulo, String mensaje, Long canalId) {

        // Si el canal no es valido envia la excepcion del elseThrow
        CanalNotificacion canalValido = canalNotificacionService.getCanalNotificacionById(canalId);

        // Obtenemos al destinatario por id en usuario-service
        String url = usuariosUrl + "/api/usuarios/" + destinatarioId;
        Notificacion notificacion;

        try {
        // Si el cliente no existe es decir la url nos devuelve un (Not Found 404) lo capturamos como un error
        // En caso de que no nos devuelva un Not Found 404 es porque el Cliente si existe por lo tanto continuamos con normalidad

        // No hace nada pero ayuda a capturar el error y verificar que el cliente exista
        @SuppressWarnings("unused")
        ClienteDTO cliente = restTemplate.getForObject(url, ClienteDTO.class);

        notificacion = new Notificacion(null, destinatarioId, canalValido, titulo, mensaje, LocalDateTime.now(), true);
        } catch (HttpClientErrorException.NotFound exNotFound) {
            log.warn("Destinatario {} no encontrado, notificacion marcada como no enviada.", destinatarioId);
            notificacion = new Notificacion(null, destinatarioId, canalValido, titulo, mensaje, LocalDateTime.now(), false);
        } catch (ResourceAccessException e) {
            log.warn("Servicio de usuarios no disponible al enviar notificacion a {}: {}", destinatarioId, e.getMessage());
            notificacion = new Notificacion(null, destinatarioId, canalValido, titulo, mensaje, LocalDateTime.now(), false);
        } catch (Exception ex) {
            log.error("Error inesperado al validar destinatario {}: {}", destinatarioId, ex.getMessage());
            notificacion = new Notificacion(null, destinatarioId, canalValido, titulo, mensaje, LocalDateTime.now(), false);
        }
        return notificacionRepository.save(notificacion);
    }

}
