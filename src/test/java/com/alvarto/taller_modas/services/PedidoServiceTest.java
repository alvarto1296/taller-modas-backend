package com.alvarto.taller_modas.services;

import com.alvarto.taller_modas.Enums.EstadoPedido;
import com.alvarto.taller_modas.dtos.ItemPedidoDTO;
import com.alvarto.taller_modas.models.*;
import com.alvarto.taller_modas.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private InventarioRepository inventarioRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private User testUser;
    private Producto testProducto;
    private Inventario testInventario;
    private ItemPedidoDTO itemPedidoDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUserName("testuser");

        testProducto = new Producto();
        testProducto.setId(1L);
        testProducto.setNombre("Camisa");
        testProducto.setPrecio(new BigDecimal("25.50"));

        testInventario = new Inventario();
        testInventario.setId(1L);
        testInventario.setProducto(testProducto);
        testInventario.setCantidad(10);

        itemPedidoDTO = new ItemPedidoDTO();
        itemPedidoDTO.setProductId(1L);
        itemPedidoDTO.setCantidad(2);
    }

    @Test
    @DisplayName("crearPedido: Debe crear un pedido exitosamente con stock suficiente")
    void whenCrearPedido_withSufficientStock_thenCreatesPedido() {
        // GIVEN
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(testProducto));
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(testInventario));
        // Capturamos el pedido que se va a guardar para poder asignarle un ID simulado
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(1L); // Simulamos que la BD le asigna un ID
            return pedido;
        });

        // WHEN
        Pedido pedidoCreado = pedidoService.crearPedido("user-123", Collections.singletonList(itemPedidoDTO));

        // THEN
        assertThat(pedidoCreado).isNotNull();
        assertThat(pedidoCreado.getId()).isEqualTo(1L);
        assertThat(pedidoCreado.getUser()).isEqualTo(testUser);
        assertThat(pedidoCreado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        // 25.50 * 2 = 51.00
        assertThat(pedidoCreado.getTotal()).isEqualTo(new BigDecimal("51.00").doubleValue());
        assertThat(pedidoCreado.getDetalles()).hasSize(1);
        assertThat(pedidoCreado.getDetalles().get(0).getCantidad()).isEqualTo(2);

        // Verificar que el stock del inventario se actualizó
        ArgumentCaptor<Inventario> inventarioCaptor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository, times(1)).save(inventarioCaptor.capture());
        assertThat(inventarioCaptor.getValue().getCantidad()).isEqualTo(8); // 10 - 2

        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("crearPedido: Debe lanzar una excepción si no hay stock suficiente")
    void whenCrearPedido_withInsufficientStock_thenThrowsException() {
        // GIVEN
        itemPedidoDTO.setCantidad(11); // Pedimos más de lo que hay
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(testProducto));
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(testInventario));

        // WHEN & THEN
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            pedidoService.crearPedido("user-123", Collections.singletonList(itemPedidoDTO));
        });

        assertThat(thrown.getMessage()).contains("No hay suficiente stock para el producto: Camisa");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelarPedido: Debe cancelar un pedido y reponer el stock")
    void whenCancelarPedido_thenUpdatesStatusAndRestocksInventory() {
        // GIVEN
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(testProducto);
        detalle.setCantidad(2);

        Pedido pedidoExistente = new Pedido();
        pedidoExistente.setId(1L);
        pedidoExistente.setEstado(EstadoPedido.PENDIENTE);
        pedidoExistente.setDetalles(List.of(detalle));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoExistente));
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(testInventario));

        // WHEN
        pedidoService.cancelarPedido(1L);

        // THEN
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository, times(1)).save(pedidoCaptor.capture());
        assertThat(pedidoCaptor.getValue().getEstado()).isEqualTo(EstadoPedido.CANCELADO);

        ArgumentCaptor<Inventario> inventarioCaptor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository, times(1)).save(inventarioCaptor.capture());
        assertThat(inventarioCaptor.getValue().getCantidad()).isEqualTo(12); // 10 + 2
    }

    @Test
    @DisplayName("cancelarPedido: Debe lanzar una excepción si el pedido ya fue entregado")
    void whenCancelarPedido_ifAlreadyDelivered_thenThrowsException() {
        // GIVEN
        Pedido pedidoEntregado = new Pedido();
        pedidoEntregado.setId(1L);
        pedidoEntregado.setEstado(EstadoPedido.ENTREGADO);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntregado));

        // WHEN & THEN
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            pedidoService.cancelarPedido(1L);
        });

        assertThat(thrown.getMessage()).isEqualTo("No se puede cancelar un pedido en estado ENTREGADO");
        verify(inventarioRepository, never()).save(any());
    }
}