package com.ecomarket.gestiontiendaservice.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa el resumen de una transacción devuelto por proceso-pago-service.
 * Basado en el diagrama: TransaccionPago { id, pedidoId, clienteId, montoTotal, estado }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionResumenDTO {

    private Long id;
    private Long pedidoId;
    private Long clienteId;
    private Double montoTotal;
    private String estado;
    private LocalDateTime codigoAutorizacion;
}
