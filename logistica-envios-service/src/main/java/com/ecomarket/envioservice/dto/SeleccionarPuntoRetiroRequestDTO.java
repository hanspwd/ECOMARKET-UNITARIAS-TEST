package com.ecomarket.envioservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeleccionarPuntoRetiroRequestDTO {

    @NotNull(message = "El ID del punto de retiro es obligatorio.")
    private Long puntoRetiroId;

    @NotBlank(message = "La firma de quien recibe es obligatoria.")
    private String firmaRecibe;
}
