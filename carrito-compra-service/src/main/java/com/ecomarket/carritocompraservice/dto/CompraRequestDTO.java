package com.ecomarket.carritocompraservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRequestDTO {
    private Long clienteId;
    private Long metodoEnvioId;
    private Long direccionId;
    private Long metodoPagoId;
}
