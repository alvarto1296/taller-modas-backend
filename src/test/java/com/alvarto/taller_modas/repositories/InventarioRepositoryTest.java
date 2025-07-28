package com.alvarto.taller_modas.repositories;

import com.alvarto.taller_modas.models.Categoria;
import com.alvarto.taller_modas.models.Inventario;
import com.alvarto.taller_modas.models.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InventarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventarioRepository inventarioRepository;

    private Producto producto1;
    private Inventario inventario2;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setNombre("General");
        entityManager.persist(categoria);

        producto1 = new Producto();
        producto1.setNombre("Producto A");
        producto1.setPrecio(new BigDecimal("10.00"));
        producto1.setCategoria(categoria);
        entityManager.persist(producto1);

        Producto producto2 = new Producto();
        producto2.setNombre("Producto B");
        producto2.setPrecio(new BigDecimal("20.00"));
        producto2.setCategoria(categoria);
        entityManager.persist(producto2);

        Inventario inventario1 = new Inventario();
        inventario1.setProducto(producto1);
        inventario1.setCantidad(100);
        inventario1.setNivelMinimo(20);
        entityManager.persist(inventario1);

        inventario2 = new Inventario();
        inventario2.setProducto(producto2);
        inventario2.setCantidad(15);
        inventario2.setNivelMinimo(20);
        entityManager.persist(inventario2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Debe encontrar inventario por ID de producto")
    void whenFindByProductoId_thenReturnInventario() {
        // When
        Optional<Inventario> foundInventario = inventarioRepository.findByProductoId(producto1.getId());

        // Then
        assertThat(foundInventario).isPresent();
        assertThat(foundInventario.get().getProducto().getId()).isEqualTo(producto1.getId());
    }

    @Test
    @DisplayName("Debe encontrar productos con bajo stock")
    void whenFindBajoStock_thenReturnInventarios() {
        // When
        List<Inventario> bajoStock = inventarioRepository.findBajoStock();

        // Then
        assertThat(bajoStock).hasSize(1);
        assertThat(bajoStock.get(0).getId()).isEqualTo(inventario2.getId());
    }
}