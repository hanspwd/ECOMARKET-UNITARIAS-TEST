package com.ecomarket.pedidos.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDTO {
    private Long id;
    private Long clienteId;
    private Double subtotal;
    private List<ItemCarritoDTO> items;
}
