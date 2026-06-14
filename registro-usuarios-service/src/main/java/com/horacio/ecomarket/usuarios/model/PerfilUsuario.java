package com.horacio.ecomarket.usuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfil_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotNull
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String correo;


    private String telefono;

    private LocalDateTime fechaCreacion;

    // Relación con Rol
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    // Relación con EstadoPerfil
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_perfil_id")
    private EstadoPerfil estadoPerfil;

    // Relación con Permiso (muchos a muchos)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "perfil_permiso",
        joinColumns = @JoinColumn(name = "perfil_usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private List<Permiso> permisos = new ArrayList<>();
}
