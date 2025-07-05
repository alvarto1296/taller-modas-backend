package com.alvarto.taller_modas.services;

import com.alvarto.taller_modas.Enums.EstadoPedido;
import com.alvarto.taller_modas.dtos.ItemPedidoDTO;
import com.alvarto.taller_modas.models.*;
import com.alvarto.taller_modas.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository; // Necesario para obtener productos por ID
    private final UserRepository userRepository; // Necesario para obtener el usuario que hace el pedido

    
    public PedidoService(PedidoRepository pedidoRepository,
                         DetallePedidoRepository detallePedidoRepository,
                         InventarioRepository inventarioRepository,
                         ProductoRepository productoRepository,
                         UserRepository userRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea un nuevo pedido verificando la disponibilidad en el inventario.
     *
     * @param userId El ID del usuario que realiza el pedido.
     * @param items Lista de DTOs con productId y cantidad para cada item del pedido.
     * @return El pedido creado.
     * @throws RuntimeException Si el producto no existe, no hay suficiente stock o el usuario no existe.
     */
    @Transactional
    public Pedido crearPedido(String userId, List<ItemPedidoDTO> items) {
        // 1. Verificar y obtener el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        Pedido pedido = new Pedido();
        pedido.setUser(user);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(0.0); // Se calculará después de añadir los detalles

        List<DetallePedido> detalles = new ArrayList<>();
        double totalPedido = 0.0;

        // 2. Procesar cada ítem del pedido
        for (ItemPedidoDTO itemDto : items) {
            Producto producto = productoRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + itemDto.getProductId()));

            Inventario inventario = inventarioRepository.findByProductoId(producto.getId())
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado para el producto: " + producto.getNombre()));

            // Validar stock
            if (inventario.getCantidad() < itemDto.getCantidad()) {
                throw new RuntimeException("No hay suficiente stock para el producto: " + producto.getNombre() + ". Stock disponible: " + inventario.getCantidad());
            }

            // Crear detalle del pedido
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio()); // Guarda el precio en el momento del pedido
            detalles.add(detalle);

            totalPedido += producto.getPrecio().doubleValue() * itemDto.getCantidad();


            // Actualizar inventario (restar la cantidad pedida)
            inventario.setCantidad(inventario.getCantidad() - itemDto.getCantidad());
            inventario.setUltimaActualizacion(LocalDateTime.now()); // Actualizar la fecha de modificación del inventario
            inventarioRepository.save(inventario); // Guardar el inventario actualizado
        }

        pedido.setDetalles(detalles);
        pedido.setTotal(totalPedido);

        // 3. Guardar el pedido y sus detalles
        // Al tener cascade = CascadeType.ALL en Pedido para 'detalles',
        // al guardar el pedido, los detalles se guardarán automáticamente.
        return pedidoRepository.save(pedido);
    }

    /**
     * Busca un pedido por su ID.
     * @param id ID del pedido.
     * @return Optional de Pedido.
     */
    public Optional<Pedido> buscarPedidoPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    /**
     * Lista todos los pedidos.
     * @return Lista de todos los pedidos.
     */
    public List<Pedido> listarTodosLosPedidos() {
        return pedidoRepository.findAll();
    }

    /**
     * Lista los pedidos de un usuario específico.
     * @param userId ID del usuario.
     * @return Lista de pedidos del usuario.
     * @throws RuntimeException Si el usuario no existe.
     */
    public List<Pedido> listarPedidosPorUsuario(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        return pedidoRepository.findByUser(user);
    }

    /**
     * Actualiza el estado de un pedido.
     * @param pedidoId ID del pedido a actualizar.
     * @param nuevoEstado Nuevo estado del pedido.
     * @return El pedido actualizado.
     * @throws RuntimeException Si el pedido no es encontrado.
     */
    public Pedido actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    /**
     * Cancela un pedido y devuelve los productos al inventario.
     * @param pedidoId ID del pedido a cancelar.
     * @throws RuntimeException Si el pedido no es encontrado o ya está en un estado final.
     */
    @Transactional
    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new RuntimeException("No se puede cancelar un pedido en estado " + pedido.getEstado());
        }

        // Devolver productos al inventario
        for (DetallePedido detalle : pedido.getDetalles()) {
            Inventario inventario = inventarioRepository.findByProductoId(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado para el producto: " + detalle.getProducto().getNombre()));
            inventario.setCantidad(inventario.getCantidad() + detalle.getCantidad());
            inventario.setUltimaActualizacion(LocalDateTime.now());
            inventarioRepository.save(inventario);
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }
}