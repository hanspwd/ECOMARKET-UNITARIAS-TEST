package com.ecomarket.carritocompraservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnadirProductoRequestDTO {
    private Long clienteId;
    private Long productoId;
    private Integer cantidad;
}
