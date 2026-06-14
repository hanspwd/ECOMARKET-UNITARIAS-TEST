package com.ecomarket.pedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long clienteId;

    @NotNull
    @PositiveOrZero
    private Double subtotal;

    @NotNull
    @PositiveOrZero
    private Double total;

    private Long direccionEnvioId;

    @ManyToOne
    @JoinColumn(name = "estado_pedido_id")
    private EstadoPedido estado;

    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> items;
}
