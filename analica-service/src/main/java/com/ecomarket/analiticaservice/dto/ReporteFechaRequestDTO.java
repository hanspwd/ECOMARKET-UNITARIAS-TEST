package com.ecomarket.analiticaservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteFechaRequestDTO {

    @NotNull(message = "El ID del solicitante es obligatorio.")
    private Long solicitanteId;

    @NotNull(message = "El ID del tipo reporte es obligatorio.")
    private Long tipoReporteId;

    @NotNull(message = "La fecha inicio es obligatoria.")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha fin es obligatoria.")
    private LocalDateTime fechaFin;
}
