package com.ecomarket.analiticaservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvioDTO {
    private Long id;
    private Long pedidoId;
    private Long transportistaId;
    private String estado;
    private String metodoEnvio;
    private LocalDateTime fechaCreacion;
}
