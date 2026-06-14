package com.ecomarket.analiticaservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccionLogDTO {
    private String microservicio;
    private String accion;
    private Long usuarioId;
    private String detalles;
    private java.time.LocalDateTime fecha;
}
