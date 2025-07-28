package com.alvarto.taller_modas.repositories;

import com.alvarto.taller_modas.models.Categoria;
import com.alvarto.taller_modas.models.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductoRepository productoRepository;

    private Categoria categoria1;
    private Categoria categoria2;
    private Producto producto1;
    private Producto producto2;
    private Producto producto3;

    @BeforeEach
    void setUp() {
        categoria1 = new Categoria();
        categoria1.setNombre("Camisas");
        entityManager.persist(categoria1);

        categoria2 = new Categoria();
        categoria2.setNombre("Pantalones");
        entityManager.persist(categoria2);

        producto1 = new Producto();
        producto1.setNombre("Camisa de Lino");
        producto1.setDescripcion("Camisa fresca de lino");
        producto1.setPrecio(new BigDecimal("49.99"));
        producto1.setColor("Blanco");
        producto1.setTalla("M");
        producto1.setCategoria(categoria1);
        entityManager.persist(producto1);

        producto2 = new Producto();
        producto2.setNombre("Pantalón Vaquero");
        producto2.setDescripcion("Pantalón de mezclilla");
        producto2.setPrecio(new BigDecimal("79.99"));
        producto2.setColor("Azul");
        producto2.setTalla("L");
        producto2.setCategoria(categoria2);
        entityManager.persist(producto2);
        
        producto3 = new Producto();
        producto3.setNombre("Camisa de Algodón");
        producto3.setDescripcion("Camisa de algodón suave");
        producto3.setPrecio(new BigDecimal("39.99"));
        producto3.setColor("Azul");
        producto3.setTalla("M");
        producto3.setCategoria(categoria1);
        entityManager.persist(producto3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Debe encontrar productos por ID de categoría")
    void whenFindByCategoriaId_thenReturnProductos() {
        // When
        List<Producto> productos = productoRepository.findByCategoriaId(categoria1.getId());

        // Then
        assertThat(productos).hasSize(2);
        assertThat(productos).extracting(Producto::getNombre).containsExactlyInAnyOrder("Camisa de Lino", "Camisa de Algodón");
    }

    @Test
    @DisplayName("Debe encontrar productos por nombre que contenga una cadena, ignorando mayúsculas")
    void whenFindByNombreContainingIgnoreCase_thenReturnProductos() {
        // When
        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCase("camisa");

        // Then
        assertThat(productos).hasSize(2);
        assertThat(productos).extracting(Producto::getNombre).containsExactlyInAnyOrder("Camisa de Lino", "Camisa de Algodón");
    }

    @Test
    @DisplayName("Debe filtrar productos por categoría, color y talla")
    void whenFiltrarProductos_thenReturnMatchingProductos() {
        // Asumo que el método filtrarProductos ignora los parámetros nulos.
        // Caso 1: filtrar por categoría y color
        List<Producto> productos = productoRepository.filtrarProductos(categoria1.getId(), "Azul", null);
        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).getNombre()).isEqualTo("Camisa de Algodón");

        // Caso 2: filtrar solo por color
        List<Producto> productos4 = productoRepository.filtrarProductos(null, "Azul", null);
        assertThat(productos4).hasSize(2);
        assertThat(productos4).extracting(Producto::getNombre).containsExactlyInAnyOrder("Pantalón Vaquero", "Camisa de Algodón");
    }
}