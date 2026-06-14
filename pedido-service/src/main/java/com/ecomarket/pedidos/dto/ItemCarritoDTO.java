package com.ecomarket.pedidos.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarritoDTO {
    private Long productoId;
    private Integer cantidad;
    private Double precioUnitarioAgregado;
}
