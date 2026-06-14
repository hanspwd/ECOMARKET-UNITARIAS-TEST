package com.ecomarket.envioservice.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ruta_transporte")
public class RutaTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del transportista es obligatorio.")
    @Column(nullable = false)
    private Long transportistaId;

    @NotNull(message = "La fecha de la ruta es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRuta;

    @NotNull(message = "El estado completada es obligatorio.")
    @Column(nullable = false)
    private Boolean completada;

    @ElementCollection
    @CollectionTable(name = "ruta_envio", joinColumns = @JoinColumn(name = "ruta_id"))
    @Column(name = "envio_id")
    private List<Long> enviosIds = new ArrayList<>();
}
