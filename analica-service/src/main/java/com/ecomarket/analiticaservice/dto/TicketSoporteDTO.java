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
public class TicketSoporteDTO {
    private Long id;
    private Long clienteId;
    private String asunto;
    private String categoria;
    private String estado;
    private LocalDateTime fechaCreacion;
}
