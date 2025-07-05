package com.alvarto.taller_modas.repositories;

import com.alvarto.taller_modas.models.DetallePedido;
import com.alvarto.taller_modas.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    List<DetallePedido> findByPedido(Pedido pedido);
}