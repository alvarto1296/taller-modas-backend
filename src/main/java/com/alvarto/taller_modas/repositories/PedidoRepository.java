package com.alvarto.taller_modas.repositories;

import com.alvarto.taller_modas.models.Pedido;
import com.alvarto.taller_modas.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUser(User user);
    
}