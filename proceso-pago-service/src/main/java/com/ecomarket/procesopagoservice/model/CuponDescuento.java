package com.ecomarket.procesopagoservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "cupon_descuento")
@Data @NoArgsConstructor @AllArgsConstructor
public class CuponDescuento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    private String codigo;

    @NotNull
    @PositiveOrZero
    private Double porcentajeDescuento;

    @PositiveOrZero
    private Double montoMaximoDescuento;

    @NotNull
    private LocalDateTime fechaExpiracion;

    @NotNull
    private Boolean activo;

    public Boolean esValido() {
        return activo != null && activo
                && fechaExpiracion != null
                && LocalDateTime.now().isBefore(fechaExpiracion);
    }
}
