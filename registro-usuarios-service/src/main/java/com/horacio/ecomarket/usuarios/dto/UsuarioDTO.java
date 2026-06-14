package com.horacio.ecomarket.usuarios.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UsuarioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Email(message = "Correo inválido")
    @NotBlank
    private String correo;

    @Size(min = 5, message = "La contraseña debe tener mínimo 5 caracteres")
    private String password;

    private String telefono;
}