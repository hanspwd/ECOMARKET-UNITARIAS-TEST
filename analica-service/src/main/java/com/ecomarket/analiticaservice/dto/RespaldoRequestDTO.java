package com.ecomarket.analiticaservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespaldoRequestDTO {

    @NotNull(message = "El ID del estado respaldo es obligatorio.")
    private Long estadoRespaldoId;

    private Double tamanoMegabytes;

    private String rutaAlmacenamiento;
}
