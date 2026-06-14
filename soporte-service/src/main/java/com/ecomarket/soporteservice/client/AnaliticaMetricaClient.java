package com.ecomarket.soporteservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class AnaliticaMetricaClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservicio.analitica.url}")
    private String analiticaUrl;

    private static final Logger log = LoggerFactory.getLogger(AnaliticaMetricaClient.class);

    public void registrarMetrica(String clave, double valorNumerico, String valorTexto) {
        try {
            String url = analiticaUrl + "/api/v1/metricas";
            var request = new java.util.HashMap<String, Object>();
            request.put("claveMetrica", clave);
            request.put("valorNumerico", valorNumerico);
            request.put("valorTexto", valorTexto);
            restTemplate.postForEntity(url, request, String.class);
            log.info("Metrica {} registrada en analiticaservice", clave);
        } catch (ResourceAccessException e) {
            log.warn("analiticaservice no disponible al registrar metrica: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar metrica en analiticaservice: {}", e.getMessage());
        }
    }
}
