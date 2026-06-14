package com.ecomarket.carritocompraservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoClienteDTO {
    private Long id;
    private String sku;
    private String nombre;
    private Double precioBase;
    private String descripcion;
    private String imagenUrl;
}
