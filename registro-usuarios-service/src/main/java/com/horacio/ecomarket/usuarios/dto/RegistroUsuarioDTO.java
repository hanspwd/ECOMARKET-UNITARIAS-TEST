package com.horacio.ecomarket.usuarios.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroUsuarioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Email(message = "Correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @Size(min = 5, message = "La contraseña debe tener mínimo 5 caracteres")
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenaInicial;

    private String telefono;

    private Long rolId;

    private Long estadoPerfilId;
}
