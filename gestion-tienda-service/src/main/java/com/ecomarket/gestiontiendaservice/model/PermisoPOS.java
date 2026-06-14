package com.ecomarket.gestiontiendaservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "permiso_pos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermisoPOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long rolEmpleado;

    @NotNull
    private Boolean permiteAnulaciones;

    @NotNull
    private Boolean permiteAperturaCaja;

    @NotNull
    private Boolean permiteAplicarDescuentoManual;
}