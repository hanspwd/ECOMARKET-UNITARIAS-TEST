package com.ecomarket.soporteservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoporteTicketRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio.")
    private Long clienteId;

    @NotNull(message = "El ID de la categoria es obligatorio.")
    private Long categoriaId;

    @NotBlank(message = "El asunto es obligatorio.")
    private String asunto;

    @NotNull(message = "El ID del pedido es obligatorio.")
    private Long pedidoId;
    
}
