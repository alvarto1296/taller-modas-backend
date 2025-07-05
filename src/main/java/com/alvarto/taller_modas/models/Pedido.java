package com.alvarto.taller_modas.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.alvarto.taller_modas.Enums.EstadoPedido;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Relación con el usuario que hace el pedido
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Asumiendo que tienes un modelo User

    @Column(nullable = false)
    private LocalDateTime fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado; // Enum para el estado del pedido

    @Column(nullable = false)
    private Double total; // Suma de los precios de los detalles del pedido

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles;

    // Puedes añadir más campos como dirección de envío, etc.

    @PrePersist
    protected void onCreate() {
        fechaPedido = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoPedido.PENDIENTE; // Estado inicial del pedido
        }
    }
}