package com.ecomarket.carritocompraservice.dto;

import com.ecomarket.carritocompraservice.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraResultDTO {
    private Long carritoId;
    private Pedido pedido;
    private Long transaccionPagoId;
    private Long envioId;
    private String estado;
}
