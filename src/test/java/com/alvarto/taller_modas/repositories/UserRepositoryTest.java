package com.alvarto.taller_modas.repositories;

import com.alvarto.taller_modas.Enums.RoleList;
import com.alvarto.taller_modas.models.Role; // Necesitarás importar la clase Role
import com.alvarto.taller_modas.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Para persistir entidades directamente en el contexto de la prueba

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Role testRole; // Agregamos un campo para el Role de prueba

    @BeforeEach
    void setUp() {
        // 1. Crear y persistir un Role primero
        testRole = new Role();
        //testRole.setId(1); // Asegúrate de que el ID sea apropiado para tu estrategia de generación de ID para Role
        testRole.setName(RoleList.ROLE_USER); // O el nombre de rol que consideres
        entityManager.persistAndFlush(testRole); // Persistimos el rol para que exista en la DB

        // 2. Ahora, crear y asociar el User con el Role
        testUser = new User();
        //testUser.setId("user123"); // Si usas UUID, este ID se generaría automáticamente si no lo seteas
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(testRole); // ¡Asociar el User con el Role persistido!

        entityManager.persistAndFlush(testUser); // Persistimos el usuario
    }

    @Test
    @DisplayName("Debe encontrar un usuario por nombre de usuario")
    void whenFindByUserName_thenReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByUserName("testUser");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserName()).isEqualTo(testUser.getUserName());
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(foundUser.get().getRole().getName()).isEqualTo(testRole.getName()); // Puedes verificar también el rol
    }

    @Test
    @DisplayName("No debe encontrar un usuario por nombre de usuario inexistente")
    void whenFindByNonExistentUserName_thenReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByUserName("nonExistentUser");

        // Then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("Debe verificar si un nombre de usuario existe")
    void whenExistsByUserName_thenReturnTrue() {
        // When
        boolean exists = userRepository.existsByUserName("testUser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe verificar si un nombre de usuario no existe")
    void whenExistsByNonExistentUserName_thenReturnFalse() {
        // When
        boolean exists = userRepository.existsByUserName("nonExistentUser");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar un usuario por email")
    void whenFindByEmail_thenReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("No debe encontrar un usuario por email inexistente")
    void whenFindByNonExistentEmail_thenReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isNotPresent();
    }
}