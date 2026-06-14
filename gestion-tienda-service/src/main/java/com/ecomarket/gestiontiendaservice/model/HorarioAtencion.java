package com.ecomarket.gestiontiendaservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "horario_atencion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private Long sucursalId;

    @NotNull
    @Column
    private Integer diaSemana;

    @NotNull
    @NotBlank
    @Column
    private String horaApertura;

    @NotNull
    @NotBlank
    @Column
    private String horaCierre;

    @NotNull
    @Column
    private Boolean esFeriado;
}
