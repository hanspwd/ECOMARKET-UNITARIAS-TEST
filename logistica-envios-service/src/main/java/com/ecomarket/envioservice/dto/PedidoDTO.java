package com.ecomarket.envioservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoDTO {

    private Long id;
    private Long clienteId;
    private Double subtotal;
    private Double total;
}
