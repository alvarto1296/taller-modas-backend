package com.alvarto.taller_modas.repositories;

import com.alvarto.taller_modas.Enums.EstadoPedido;
import com.alvarto.taller_modas.models.Pedido;
import com.alvarto.taller_modas.models.Role;
import com.alvarto.taller_modas.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PedidoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PedidoRepository pedidoRepository;

    private User user1;
    private User user2;
    private Pedido pedido1;
    private Pedido pedido2;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // --- 1. Crear y Persistir un Role primero ---
        testRole = new Role();
        // NO setear ID si Role usa @GeneratedValue(strategy = GenerationType.IDENTITY) o similar
        testRole.setName(com.alvarto.taller_modas.Enums.RoleList.ROLE_USER);
        // Persistir y obtener la instancia gestionada del Role
        testRole = entityManager.persistAndFlush(testRole); // ¡Importante: reasignar la instancia gestionada!

        // --- 2. Crear y Persistir Usuarios ---
        user1 = new User();
        // user1.setId("user1"); // NO setear ID si User usa @GeneratedValue(strategy = GenerationType.UUID)
        user1.setUserName("testuser1");
        user1.setEmail("user1@example.com");
        user1.setPassword("pass");
        user1.setRole(testRole); // Asociar con la instancia *gestionada* del Role
        user1.setProvider("LOCAL"); // Asegúrate de inicializar todos los campos non-nullable
        user1.setEnabled(true); // Asegúrate de inicializar todos los campos non-nullable
        entityManager.persistAndFlush(user1);

        user2 = new User();
        // user2.setId("user2"); // NO setear ID si User usa @GeneratedValue(strategy = GenerationType.UUID)
        user2.setUserName("testuser2");
        user2.setEmail("user2@example.com");
        user2.setPassword("pass");
        user2.setRole(testRole); // Asociar con la instancia *gestionada* del Role
        user2.setProvider("LOCAL");
        user2.setEnabled(true);
        entityManager.persistAndFlush(user2);

        // --- 3. Crear y Persistir Pedidos ---
        pedido1 = new Pedido();
        pedido1.setUser(user1);
        pedido1.setFechaPedido(LocalDateTime.now());
        pedido1.setEstado(EstadoPedido.PENDIENTE);
        pedido1.setTotal(100.0);
        entityManager.persistAndFlush(pedido1);

        pedido2 = new Pedido();
        pedido2.setUser(user1);
        pedido2.setFechaPedido(LocalDateTime.now().minusDays(1));
        pedido2.setEstado(EstadoPedido.ENTREGADO);
        pedido2.setTotal(50.0);
        entityManager.persistAndFlush(pedido2);

        Pedido pedido3 = new Pedido();
        pedido3.setUser(user2);
        pedido3.setFechaPedido(LocalDateTime.now());
        pedido3.setEstado(EstadoPedido.PENDIENTE);
        pedido3.setTotal(200.0);
        entityManager.persistAndFlush(pedido3);
    }

    @Test
    @DisplayName("Debe encontrar todos los pedidos de un usuario específico")
    void whenFindByUser_thenReturnPedidos() {
        // When
        List<Pedido> pedidos = pedidoRepository.findByUser(user1);

        // Then
        assertThat(pedidos).hasSize(2);
        assertThat(pedidos).extracting(Pedido::getId).containsExactlyInAnyOrder(pedido1.getId(), pedido2.getId());
    }

    @Test
    @DisplayName("Debe devolver una lista vacía si el usuario no tiene pedidos")
    void whenFindByUserWithNoOrders_thenReturnEmptyList() {
        User userWithoutOrders = new User();
        userWithoutOrders.setUserName("noorders");
        userWithoutOrders.setEmail("noorders@example.com");
        userWithoutOrders.setPassword("pass");
        userWithoutOrders.setRole(testRole); // ¡Importante! Asegúrate de asociar un rol
        userWithoutOrders.setProvider("LOCAL");
        userWithoutOrders.setEnabled(true);
        entityManager.persistAndFlush(userWithoutOrders);

        List<Pedido> pedidos = pedidoRepository.findByUser(userWithoutOrders);
        assertThat(pedidos).isEmpty();
    }
}