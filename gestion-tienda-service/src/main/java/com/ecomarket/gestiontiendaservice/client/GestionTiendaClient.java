package com.ecomarket.gestiontiendaservice.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GestionTiendaClient {

    private RestTemplate restTemplate;

    @Value("${microservicio.gestiontienda.url}")
    private String baseUrl;

    public GestionTiendaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void notificarStockBajo(Long sucursalId, Long productoId, Integer stockActual) {
        String url = baseUrl + "/api/gestion-tienda/alertas/stock-bajo";
        Map<String, Object> body = Map.of(
            "sucursalId", sucursalId,
            "productoId", productoId,
            "stockActual", stockActual
        );
        restTemplate.postForEntity(url, body, Void.class);
    }
}