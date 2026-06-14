package com.ecomarket.analiticaservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteRequestDTO {

    @NotNull(message = "El ID del solicitante es obligatorio.")
    private Long solicitanteId;

    @NotNull(message = "El ID del tipo reporte es obligatorio.")
    private Long tipoReporteId;
}
