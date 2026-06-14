package com.ecomarket.carritocompraservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.ecomarket.carritocompraservice.dto.MetodoPagoDTO;

@Component
public class ProcesoPagoClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservicio.pagos.url}")
    private String pagosUrl;

    private static final Logger log = LoggerFactory.getLogger(ProcesoPagoClient.class);

    public Long iniciarPago(Long pedidoId, Long clienteId, Double monto, Long metodoPagoId) {
        try {
            String url = pagosUrl + "/api/pagos/iniciar?pedidoId=" + pedidoId
                    + "&clienteId=" + clienteId + "&monto=" + monto;
            MetodoPagoDTO metodo = new MetodoPagoDTO(metodoPagoId, "TARJETA");
            @SuppressWarnings("rawtypes")
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(url, metodo, java.util.Map.class);
            if (response.getBody() != null && response.getBody().get("id") != null) {
                return ((Number) response.getBody().get("id")).longValue();
            }
            log.warn("Respuesta inesperada de pagoservice al iniciar pago");
            return null;
        } catch (ResourceAccessException e) {
            log.warn("pagoservice no disponible al iniciar pago: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error al iniciar pago en pagoservice: {}", e.getMessage());
            return null;
        }
    }
}
