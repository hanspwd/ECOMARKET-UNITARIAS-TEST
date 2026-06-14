package com.ecomarket.gestiontiendaservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente REST para comunicarse con registro-usuarios-service.
 * Permite verificar que un empleado existe antes de asignarle una tarea.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RegistroUsuariosClient {

    private final RestClient restClient;

    @Value("${app.services.registro-usuarios-url}")
    private String registroUsuariosUrl;

    /**
     * Obtiene los datos de un empleado por su id.
     * GET /api/usuarios/{empleadoId}/perfil
     */
    public EmpleadoDTO obtenerEmpleado(Long empleadoId) {
        String url = registroUsuariosUrl + "/api/usuarios/" + empleadoId + "/perfil";
        try {
            log.info("Consultando empleado {} en registro-usuarios-service", empleadoId);
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new RuntimeException("Error al obtener empleado: " + empleadoId);
                    })
                    .body(EmpleadoDTO.class);
        } catch (Exception e) {
            log.error("Error al obtener empleado {}: {}", empleadoId, e.getMessage());
            throw new RuntimeException("No se pudo obtener el empleado: " + empleadoId, e);
        }
    }

    /**
     * Verifica si un empleado existe y está activo.
     * Retorna true si existe, false si falla la consulta (tolerancia a fallos).
     */
    public boolean empleadoExiste(Long empleadoId) {
        try {
            EmpleadoDTO empleado = obtenerEmpleado(empleadoId);
            return empleado != null;
        } catch (Exception e) {
            log.warn("No se pudo verificar existencia del empleado {}: {}", empleadoId, e.getMessage());
            return false;
        }
    }
}
