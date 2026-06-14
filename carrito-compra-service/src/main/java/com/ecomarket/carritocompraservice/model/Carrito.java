package com.ecomarket.carritocompraservice.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import jakarta.persistence.CascadeType;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carrito")
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long clienteId;

    @NotNull
    @PositiveOrZero
    private Double subtotal = 0.0;

    private Long tipoEnvioSeleccionadoId;

    private Long metodoPagoSeleccionadoId;

    private LocalDateTime fechaUltimaModificacion = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items;

    public Double calcularTotal() {
        if (items == null || items.isEmpty()) return 0.0;
        return items.stream()
                .mapToDouble(ItemCarrito::calcularSubtotalItem)
                .sum();
    }
}
