package com.ecomarket.carritocompraservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class LogisticaEnvioClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservicio.envios.url}")
    private String enviosUrl;

    private static final Logger log = LoggerFactory.getLogger(LogisticaEnvioClient.class);

    public Long crearEnvio(Long pedidoId, Long clienteId, Long metodoEnvioId, Long direccionId) {
        try {
            String url = enviosUrl + "/api/v1/logistica-envios/envios";
            var request = new java.util.HashMap<String, Object>();
            request.put("pedidoId", pedidoId);
            request.put("clienteId", clienteId);
            request.put("metodoEnvioId", metodoEnvioId);
            request.put("direccionId", direccionId);
            @SuppressWarnings("rawtypes")
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(url, request, java.util.Map.class);
            if (response.getBody() != null && response.getBody().get("id") != null) {
                return ((Number) response.getBody().get("id")).longValue();
            }
            log.warn("Respuesta inesperada de envioservice al crear envio");
            return null;
        } catch (ResourceAccessException e) {
            log.warn("envioservice no disponible al crear envio: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error al crear envio en envioservice: {}", e.getMessage());
            return null;
        }
    }
}
