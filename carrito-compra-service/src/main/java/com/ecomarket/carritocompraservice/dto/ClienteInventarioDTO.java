package com.ecomarket.carritocompraservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteInventarioDTO {
    private Long id;
    private Long productoId;
    private Long sucursalId;
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
    private Integer stockMinimoAlerta;
}
