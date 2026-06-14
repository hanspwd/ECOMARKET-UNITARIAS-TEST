package com.ecomarket.soporteservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResenaRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio.")
    private Long productoId;

    @NotNull(message = "El ID del cliente es obligatorio.")
    private Long clienteId;

    @NotNull(message = "La calificacion es obligatoria.")
    @Min(value = 1, message = "La calificacion minima es 1 estrella.")
    @Max(value = 5, message = "La calificacion maxima es 5 estrellas.")
    private Integer calificacionEstrellas;

    @NotBlank(message = "El comentario no puede estar vacio.")
    private String comentario;
}
