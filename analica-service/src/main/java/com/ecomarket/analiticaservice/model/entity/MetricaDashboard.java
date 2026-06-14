package com.ecomarket.analiticaservice.model.entity;

import java.time.LocalDateTime;

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
@Table(name = "metrica_dashboard")
public class MetricaDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La clave metrica es obligatoria.")
    @Column(nullable = false, length = 100, unique = true)
    private String claveMetrica;

    private Double valorNumerico;

    @Column(length = 500)
    private String valorTexto;

    @NotNull(message = "La ultima actualizacion es obligatoria.")
    @Column(nullable = false)
    private LocalDateTime ultimaActualizacion;
}
