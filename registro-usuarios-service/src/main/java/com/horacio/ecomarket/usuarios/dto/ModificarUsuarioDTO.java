package com.horacio.ecomarket.usuarios.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ModificarUsuarioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Email(message = "Correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    private String telefono;

    private Long rolId;

    private Long estadoPerfilId;
}
