package com.ecomarket.analiticaservice.model.entity;

import java.time.LocalDateTime;

import com.ecomarket.analiticaservice.model.reference.NivelAlerta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "alerta_sistema")
public class AlertaSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nivel de alerta es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "nivel_alerta_id")
    private NivelAlerta nivel;

    @NotBlank(message = "El mensaje es obligatorio.")
    @Column(nullable = false, length = 1000)
    private String mensaje;

    @NotBlank(message = "El modulo origen es obligatorio.")
    @Column(nullable = false, length = 100)
    private String moduloOrigen;

    @NotNull(message = "La fecha de alerta es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAlerta;

    @NotNull(message = "El estado resuelta es obligatorio.")
    @Column(nullable = false)
    private Boolean resuelta;
}
