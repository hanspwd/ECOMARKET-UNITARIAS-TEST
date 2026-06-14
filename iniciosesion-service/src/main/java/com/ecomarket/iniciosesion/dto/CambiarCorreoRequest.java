package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarCorreoRequest {

    @NotNull(message = "El usuarioId es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El nuevo correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String nuevoCorreo;

    @NotBlank(message = "La contraseña actual es obligatoria para confirmar el cambio")
    private String contrasenaActual;
}
