package com.ecomarket.analiticaservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarritoDTO {
    private Long id;
    private Long clienteId;
    private Long productoId;
    private Integer cantidad;
    private Double precioUnitario;
}
