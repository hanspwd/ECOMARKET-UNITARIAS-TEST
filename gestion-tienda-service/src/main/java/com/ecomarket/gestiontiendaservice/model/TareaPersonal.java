package com.ecomarket.gestiontiendaservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarea_personal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private Long empleadoId;

    @Column
    private Long gerenteAsignadoId;

    @NotNull
    @Column
    private Long sucursalId;

    @NotNull
    @NotBlank
    @Column
    private String titulo;

    @Column
    private String descripcion;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "estado_id")
    private EstadoTareaPersonal estado;

    @NotNull
    @Column
    private LocalDateTime fechaAsignacion;

    @Column
    private LocalDateTime fechaLimite;
}