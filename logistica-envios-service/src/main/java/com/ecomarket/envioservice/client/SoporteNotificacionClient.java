package com.ecomarket.envioservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SoporteNotificacionClient {

    private final RestTemplate restTemplate;

    @Value("${microservicio.soporte.url}")
    private String soporteUrl;

    private static final Logger log = LoggerFactory.getLogger(SoporteNotificacionClient.class);

    public void notificarCreacionEnvio(Long destinatarioId, Long pedidoId, Long envioId) {
        try {
            String url = soporteUrl + "/api/v1/soporte/enviar-notificacion-push";
            var request = new java.util.HashMap<String, Object>();
            request.put("destinatarioId", destinatarioId);
            request.put("titulo", "Envio creado exitosamente");
            request.put("mensaje", "Tu envio #" + envioId + " para el pedido #" + pedidoId + " ha sido creado.");
            request.put("canalId", 1);
            restTemplate.postForEntity(url, request, String.class);
            log.info("Notificacion de envio creado enviada a soporteservice para cliente {}", destinatarioId);
        } catch (ResourceAccessException e) {
            log.warn("soporteservice no disponible al enviar notificacion: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error al notificar a soporteservice: {}", e.getMessage());
        }
    }
}
