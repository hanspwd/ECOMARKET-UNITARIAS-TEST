package com.ecomarket.soporteservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificacionRequestDTO {

    @NotNull(message = "El ID del destinatario es obligatorio.")
    private Long destinatarioId;

    @NotBlank(message = "El titulo es obligatorio.")
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio.")
    private String mensaje;

    @NotNull(message = "El ID del canal es obligatorio.")
    private Long canalId;

}
