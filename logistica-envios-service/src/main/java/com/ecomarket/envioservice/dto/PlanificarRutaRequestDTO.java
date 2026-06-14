package com.ecomarket.envioservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanificarRutaRequestDTO {

    @NotNull(message = "El ID del transportista es obligatorio.")
    private Long transportistaId;

    @NotNull(message = "La lista de IDs de envios es obligatoria.")
    private List<Long> enviosIds;
}
