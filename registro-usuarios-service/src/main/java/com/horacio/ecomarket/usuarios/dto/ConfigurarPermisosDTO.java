package com.horacio.ecomarket.usuarios.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ConfigurarPermisosDTO {

    @NotNull(message = "La lista de permisos no puede ser nula")
    private List<Long> permisoIds;
}
