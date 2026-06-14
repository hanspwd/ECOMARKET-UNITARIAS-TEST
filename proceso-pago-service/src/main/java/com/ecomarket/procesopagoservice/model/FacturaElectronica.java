package com.ecomarket.procesopagoservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura_electronica")
@Data @NoArgsConstructor @AllArgsConstructor
public class FacturaElectronica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long folioFiscal;

    @NotNull
    private Long transaccionId;

    @NotNull
    private Long clienteId;

    @NotNull
    @NotBlank
    private String rutReceptor;

    @NotNull
    @NotBlank
    private String razonSocial;

    @NotNull
    private String xmlDocumento;

    @NotNull
    private LocalDateTime fechaEmision;
}
