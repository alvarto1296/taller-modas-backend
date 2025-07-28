package com.alvarto.taller_modas.services;

import com.alvarto.taller_modas.Enums.RoleList;
import com.alvarto.taller_modas.models.Role;
import com.alvarto.taller_modas.models.User;
import com.alvarto.taller_modas.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita las extensiones de Mockito para JUnit 5
class UserServiceTest {

    // @Mock crea un mock de la dependencia (UserRepository) que inyectamos en UserService
    @Mock
    private UserRepository userRepository;

    // @InjectMocks inyecta los mocks creados (@Mock) en la instancia de UserService
    // Esto crea una instancia real de UserService y le asigna el userRepository mock
    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;

    @BeforeEach // Este método se ejecuta antes de cada test
    void setUp() {
        // Inicializamos un objeto Role para asociarlo con nuestro User de prueba
        testRole = new Role();
        testRole.setId(1); // El ID es opcional para tests unitarios, pero lo incluimos por consistencia
        testRole.setName(RoleList.ROLE_USER);

        // Inicializamos un objeto User de prueba que usaremos en nuestros tests
        testUser = new User();
        testUser.setId("someUUID"); // Usamos un ID de ejemplo
        testUser.setUserName("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword"); // Contraseña simulada como si estuviera codificada
        testUser.setRole(testRole);
        testUser.setProvider("LOCAL");
        testUser.setEnabled(true);
    }


    @Test
    @DisplayName("loadUserByUsername: Debe cargar un usuario existente por nombre de usuario")
    void whenLoadUserByUsername_thenReturnUserDetails() {
        // GIVEN: Configuramos el comportamiento del userRepository mock
        // Cuando se llame a userRepository.findByUserName("testUser"),
        // devolverá un Optional que contiene a testUser.
        when(userRepository.findByUserName("testUser")).thenReturn(Optional.of(testUser));

        // WHEN: Ejecutamos el método del servicio que queremos probar
        UserDetails userDetails = userService.loadUserByUsername("testUser");

        // THEN: Verificamos el resultado
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getUserName());
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        // Verificamos que el método findByUserName del repositorio fue llamado exactamente una vez
        verify(userRepository, times(1)).findByUserName("testUser");
    }


    @Test
    @DisplayName("loadUserByUsername: Debe lanzar UsernameNotFoundException si el usuario no existe")
    void whenLoadUserByUsername_thenThrowUsernameNotFoundException() {
        // GIVEN: Configuramos el userRepository mock para que no encuentre el usuario
        when(userRepository.findByUserName("nonExistentUser")).thenReturn(Optional.empty());

        // WHEN & THEN: Verificamos que se lanza la excepción esperada
        UsernameNotFoundException thrown = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonExistentUser");
        });

        // Verificamos el mensaje de la excepción
        assertThat(thrown.getMessage()).isEqualTo("User not found");

        // Verificamos que findByUserName fue llamado una vez para el usuario inexistente
        verify(userRepository, times(1)).findByUserName("nonExistentUser");
    }


    @Test
    @DisplayName("existsByUserName: Debe devolver true si el nombre de usuario existe")
    void whenExistsByUserName_thenReturnTrue() {
        // GIVEN: Configuramos el mock para que exista el usuario
        when(userRepository.existsByUserName("testUser")).thenReturn(true);

        // WHEN: Ejecutamos el método del servicio
        boolean exists = userService.existsByUserName("testUser");

        // THEN: Verificamos el resultado
        assertThat(exists).isTrue();

        // Verificamos que el método del repositorio fue llamado
        verify(userRepository, times(1)).existsByUserName("testUser");
    }


    @Test
    @DisplayName("existsByUserName: Debe devolver false si el nombre de usuario no existe")
    void whenExistsByUserName_thenReturnFalse() {
        // GIVEN: Configuramos el mock para que el usuario no exista
        when(userRepository.existsByUserName("nonExistentUser")).thenReturn(false);

        // WHEN: Ejecutamos el método del servicio
        boolean exists = userService.existsByUserName("nonExistentUser");

        // THEN: Verificamos el resultado
        assertThat(exists).isFalse();

        // Verificamos que el método del repositorio fue llamado
        verify(userRepository, times(1)).existsByUserName("nonExistentUser");
    }


    @Test
    @DisplayName("save: Debe guardar un usuario correctamente")
    void whenSaveUser_thenRepositorySaveIsCalled() {
        // GIVEN: No necesitamos configurar un 'when' para 'save' si solo verificamos la invocación.
        // Pero si 'save' de UserRepository devolviera algo (ej. el mismo User), lo configuraríamos.
        // Por ejemplo: when(userRepository.save(any(User.class))).thenReturn(testUser);

        // WHEN: Ejecutamos el método del servicio
        userService.save(testUser);

        // THEN: Verificamos que el método 'save' del repositorio fue llamado exactamente una vez
        // con un objeto User que sea el mismo o equivalente a 'testUser'.
        verify(userRepository, times(1)).save(testUser);
    }
}