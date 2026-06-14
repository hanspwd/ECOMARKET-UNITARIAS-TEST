package com.ecomarket.soporteservice.model.entity;

import java.time.LocalDateTime;

import com.ecomarket.soporteservice.model.reference.CategoriaTicket;
import com.ecomarket.soporteservice.model.reference.EstadoTicket;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ticket_soporte")
public class TicketSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del cliente es obligatorio.")
    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = true)
    private Long empleadoAsignadoId;

    @NotNull(message = "El ID del pedido relacionado es obligatorio.")
    @Column(nullable = false)
    private Long pedidoRelacionadoId;

    @NotNull(message = "La categoria es obligatoria.")
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaTicket categoria;

    @NotBlank(message = "El asunto no puede estar vacio.")
    @Size(min = 5, max = 255, message = "El asunto debe tener entre 5 y 255 caracteres.")
    @Column(nullable = false, length = 255)
    private String asunto;

    @NotNull(message = "El estado es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "estado_id")
    private EstadoTicket estado;

    @NotNull(message = "La fecha de creacion es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = true)
    private LocalDateTime fechaCierre;

    @Column(nullable = true, length = 2000)
    private String solucionResumen;
}
