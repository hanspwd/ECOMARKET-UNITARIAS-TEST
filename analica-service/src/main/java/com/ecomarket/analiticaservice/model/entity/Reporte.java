package com.ecomarket.analiticaservice.model.entity;

import java.time.LocalDateTime;

import com.ecomarket.analiticaservice.model.reference.EstadoReporte;
import com.ecomarket.analiticaservice.model.reference.TipoReporte;

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
@Table(name = "reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del solicitante es obligatorio.")
    @Column(nullable = false)
    private Long solicitanteId;

    @NotNull(message = "El tipo de reporte es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "tipo_reporte_id")
    private TipoReporte tipo;

    @NotNull(message = "El estado del reporte es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "estado_reporte_id")
    private EstadoReporte estado;

    @NotNull(message = "La fecha de generacion es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion;

    @Column(length = 500)
    private String urlArchivoResultado;

    private Integer totalRegistrosProcesados;
}
