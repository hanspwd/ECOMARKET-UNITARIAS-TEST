package com.ecomarket.envioservice.model.entity;

import java.time.LocalDateTime;

import com.ecomarket.envioservice.model.reference.EstadoEnvio;
import com.ecomarket.envioservice.model.reference.MetodoEnvio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "envio")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del pedido es obligatorio.")
    @Column(nullable = false)
    private Long pedidoId;

    @NotNull(message = "El ID del cliente es obligatorio.")
    @Column(nullable = false)
    private Long clienteId;

    @NotNull(message = "El metodo de envio es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "metodo_envio_id")
    private MetodoEnvio metodoEnvio;

    @NotNull(message = "El estado actual es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "estado_actual_id")
    private EstadoEnvio estadoActual;

    @Column(nullable = false)
    @NotNull
    @PositiveOrZero
    private Double costoEnvio;

    @Column(nullable = false)
    @NotNull
    private Long direccionId;

    @ManyToOne
    @JoinColumn(name = "punto_retiro_id")
    private PuntoRetiro puntoRetiro;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaEstimadaEntrega;

    private LocalDateTime fechaEntregaReal;

    public LocalDateTime calcularFechaEstimada() {
        if (fechaCreacion == null) return null;
        if (metodoEnvio != null && "PuntoRetiro".equalsIgnoreCase(metodoEnvio.getNombre())) {
            return fechaCreacion.plusDays(2);
        }
        return fechaCreacion.plusDays(5);
    }
}
