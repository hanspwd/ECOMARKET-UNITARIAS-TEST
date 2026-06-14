package com.ecomarket.envioservice.model.entity;

import java.time.LocalDateTime;

import com.ecomarket.envioservice.model.reference.EstadoEnvio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "historial_envio")
public class HistorialEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del envio es obligatorio.")
    @Column(nullable = false)
    private Long envioId;

    @NotNull(message = "El estado es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "estado_id")
    private EstadoEnvio estado;

    @NotNull(message = "La fecha de actualizacion es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    @Column(length = 500)
    private String observacion;
}
