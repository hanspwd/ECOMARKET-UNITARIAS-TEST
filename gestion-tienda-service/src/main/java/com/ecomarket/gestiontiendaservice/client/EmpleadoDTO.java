package com.ecomarket.gestiontiendaservice.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO que representa un empleado/usuario devuelto por registro-usuarios-service.
 * Basado en el diagrama: PerfilUsuario { id, nombre, apellido, email, telefono }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String rol;
}
