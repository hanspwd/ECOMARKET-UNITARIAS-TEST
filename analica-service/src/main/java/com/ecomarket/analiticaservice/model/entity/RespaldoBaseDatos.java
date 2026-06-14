package com.ecomarket.analiticaservice.model.entity;

import java.time.LocalDateTime;

import com.ecomarket.analiticaservice.model.reference.EstadoRespaldo;

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
@Table(name = "respaldo_base_datos")
public class RespaldoBaseDatos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de respaldo es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRespaldo;

    private Double tamanoMegabytes;

    @NotNull(message = "El estado del respaldo es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "estado_respaldo_id")
    private EstadoRespaldo estado;

    @Column(length = 500)
    private String rutaAlmacenamiento;
}
