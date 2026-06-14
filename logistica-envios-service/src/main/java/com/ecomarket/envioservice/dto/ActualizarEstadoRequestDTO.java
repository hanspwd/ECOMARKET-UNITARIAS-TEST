package com.ecomarket.envioservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActualizarEstadoRequestDTO {

    @NotNull(message = "El ID del nuevo estado es obligatorio.")
    private Long nuevoEstadoId;

    private String observacion;
}
