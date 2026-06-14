package com.ecomarket.gestiontiendaservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Cliente REST para comunicarse con proceso-pago-service.
 * Permite consultar transacciones asociadas a una sucursal o cliente.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProcesoPagoClient {

    private final RestClient restClient;

    @Value("${app.services.proceso-pago-url}")
    private String procesoPagoUrl;

    /**
     * Obtiene el historial de transacciones de un cliente.
     * GET /api/pagos/cliente/{clienteId}
     */
    public List<TransaccionResumenDTO> obtenerTransaccionesPorCliente(Long clienteId) {
        String url = procesoPagoUrl + "/api/pagos/cliente/" + clienteId;
        try {
            log.info("Consultando transacciones del cliente {} en proceso-pago-service", clienteId);
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new RuntimeException("Error al obtener transacciones del cliente: " + clienteId);
                    })
                    .body(new ParameterizedTypeReference<List<TransaccionResumenDTO>>() {});
        } catch (Exception e) {
            log.error("Error al obtener transacciones del cliente {}: {}", clienteId, e.getMessage());
            throw new RuntimeException("No se pudo obtener las transacciones del cliente: " + clienteId, e);
        }
    }

    /**
     * Obtiene una transacción por su id.
     * GET /api/pagos/{transaccionId}
     */
    public TransaccionResumenDTO obtenerTransaccion(Long transaccionId) {
        String url = procesoPagoUrl + "/api/pagos/" + transaccionId;
        try {
            log.info("Consultando transacción {} en proceso-pago-service", transaccionId);
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new RuntimeException("Error al obtener transacción: " + transaccionId);
                    })
                    .body(TransaccionResumenDTO.class);
        } catch (Exception e) {
            log.error("Error al obtener transacción {}: {}", transaccionId, e.getMessage());
            throw new RuntimeException("No se pudo obtener la transacción: " + transaccionId, e);
        }
    }
}
