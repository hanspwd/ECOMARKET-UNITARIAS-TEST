package com.ecomarket.envioservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "direccion")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La calle es obligatoria.")
    @Column(nullable = false, length = 100)
    private String calle;

    @NotBlank(message = "El numero es obligatorio.")
    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 30)
    private String departamento;

    @NotBlank(message = "La ciudad es obligatoria.")
    @Column(nullable = false, length = 50)
    private String ciudad;

    @Column(length = 10)
    private String codigoPostal;

    private Double latitud;

    private Double longitud;
}
