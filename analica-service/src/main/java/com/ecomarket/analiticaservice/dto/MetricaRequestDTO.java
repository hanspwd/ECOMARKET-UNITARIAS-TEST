package com.ecomarket.analiticaservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricaRequestDTO {

    @NotBlank(message = "La clave metrica es obligatoria.")
    private String claveMetrica;

    private Double valorNumerico;

    private String valorTexto;
}
