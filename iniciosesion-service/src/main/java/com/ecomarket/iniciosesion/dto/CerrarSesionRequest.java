package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CerrarSesionRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;
}
