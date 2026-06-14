package com.ecomarket.gestiontiendaservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reglamento_interno")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReglamentoInterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private Long sucursalId;

    @NotNull
    @Column
    private Integer version;

    @NotNull
    @NotBlank
    @Column
    private String tituloSeccion;

    @NotNull
    @NotBlank
    @Column
    private String contenidoLegal;

    @NotNull
    @Column
    private LocalDateTime fechaVigencia;
}