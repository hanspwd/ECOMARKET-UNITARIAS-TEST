package com.horacio.ecomarket.usuarios.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_perfil")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre; // Ej: ACTIVO, INACTIVO, SUSPENDIDO

}
