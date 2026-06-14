package com.ecomarket.soporteservice.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resena")
public class Resena {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del producto es obligatorio.")
    @Column(nullable = false)
    private Long productoId;

    @NotNull(message = "El ID del cliente es obligatorio.")
    @Column(nullable = false)
    private Long clienteId;

    @NotNull(message = "La calificacion es obligatoria.")
    @Min(value = 1, message = "La calificacion minima es 1 estrella.")
    @Max(value = 10, message = "La calificacion maxima es 10 estrellas.")
    @Column(nullable = false)
    private Integer calificacionEstrellas;

    @NotBlank(message = "El comentario no puede estar vacio.")
    @Size(min = 10, max = 2000, message = "El comentario debe tener entre 10 y 2000 caracteres.")
    @Column(nullable = false, length = 2000)
    private String comentario;

    @NotNull(message = "La fecha de publicacion es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @NotNull(message = "Debe indicar si la moderacion fue aprobada.")
    @Column(nullable = false)
    private Boolean moderacionAprobado;

}
