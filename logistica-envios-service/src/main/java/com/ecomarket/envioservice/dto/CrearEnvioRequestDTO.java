package com.ecomarket.envioservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrearEnvioRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio.")
    private Long pedidoId;

    @NotNull(message = "El ID del cliente es obligatorio.")
    private Long clienteId;

    @NotNull(message = "El ID del metodo de envio es obligatorio.")
    private Long metodoEnvioId;

    @NotNull(message = "El ID de la direccion es obligatorio.")
    private Long direccionId;
}
