package com.alvarto.taller_modas.repositories;

import com.alvarto.taller_modas.models.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Categoria testCategoria;

    @BeforeEach
    void setUp() {
        // Given
        testCategoria = new Categoria();
        testCategoria.setNombre("Electrónica");
        entityManager.persistAndFlush(testCategoria);
    }

    @Test
    @DisplayName("Debe encontrar una categoría por nombre")
    void whenFindByNombre_thenReturnCategoria() {
        // When
        Optional<Categoria> foundCategoria = categoriaRepository.findByNombre("Electrónica");

        // Then
        assertThat(foundCategoria).isPresent();
        assertThat(foundCategoria.get().getNombre()).isEqualTo(testCategoria.getNombre());
    }

    @Test
    @DisplayName("No debe encontrar una categoría por nombre inexistente")
    void whenFindByNonExistentNombre_thenReturnEmpty() {
        // When
        Optional<Categoria> foundCategoria = categoriaRepository.findByNombre("Ropa");

        // Then
        assertThat(foundCategoria).isNotPresent();
    }

    @Test
    @DisplayName("Debe verificar si un nombre de categoría existe")
    void whenExistsByNombre_thenReturnTrue() {
        // When
        boolean exists = categoriaRepository.existsByNombre("Electrónica");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe verificar si un nombre de categoría no existe")
    void whenExistsByNonExistentNombre_thenReturnFalse() {
        // When
        boolean exists = categoriaRepository.existsByNombre("Ropa");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe guardar una nueva categoría")
    void whenSave_thenPersistCategoria() {
        // Given
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombre("Hogar");

        // When
        Categoria savedCategoria = categoriaRepository.save(nuevaCategoria);

        // Then
        assertThat(savedCategoria).isNotNull();
        assertThat(savedCategoria.getId()).isNotNull();
        assertThat(savedCategoria.getNombre()).isEqualTo("Hogar");
    }
}