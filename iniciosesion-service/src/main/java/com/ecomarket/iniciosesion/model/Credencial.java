package com.ecomarket.iniciosesion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "credenciales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private Long usuarioId;

    @NotNull
    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String correoAcceso;

    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String contrasenaHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean cuentaBloqueada = false;

    @Column(length = 50)
    private String rolAcceso;

    private LocalDateTime fechaUltimoLogin;
}
