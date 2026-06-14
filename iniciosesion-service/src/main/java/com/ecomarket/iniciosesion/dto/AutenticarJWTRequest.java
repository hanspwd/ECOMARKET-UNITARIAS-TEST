package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AutenticarJWTRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;
}
