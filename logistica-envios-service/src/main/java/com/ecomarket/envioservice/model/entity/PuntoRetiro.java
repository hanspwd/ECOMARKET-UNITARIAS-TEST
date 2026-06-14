package com.ecomarket.envioservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "punto_retiro")
public class PuntoRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del punto de retiro es obligatorio.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "La capacidad actual es obligatoria.")
    @Column(nullable = false)
    private Integer capacidadActual;

    @NotNull(message = "La capacidad maxima es obligatoria.")
    @Column(nullable = false)
    private Integer capacidadMaxima;

    @NotNull(message = "El estado activo es obligatorio.")
    @Column(nullable = false)
    private Boolean activo;

    public Boolean verificarDisponibilidad() {
        return activo && capacidadActual < capacidadMaxima;
    }
}
