package com.ecomarket.analiticaservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertaRequestDTO {

    @NotNull(message = "El ID del nivel alerta es obligatorio.")
    private Long nivelAlertaId;

    @NotBlank(message = "El mensaje es obligatorio.")
    private String mensaje;

    @NotBlank(message = "El modulo origen es obligatorio.")
    private String moduloOrigen;
}
