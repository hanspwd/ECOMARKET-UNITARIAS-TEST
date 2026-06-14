package com.ecomarket.iniciosesion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_jwt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionJWT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 50)
    private String rolAcceso;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;
}
