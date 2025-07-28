package com.alvarto.taller_modas.services;

import com.alvarto.taller_modas.Enums.RoleList;
import com.alvarto.taller_modas.dtos.NewUserDto;
import com.alvarto.taller_modas.jwt.JwtUtil;
import com.alvarto.taller_modas.models.Role;
import com.alvarto.taller_modas.models.User;
import com.alvarto.taller_modas.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private NewUserDto newUserDto;
    private Role userRole;

    @BeforeEach
    void setUp() {
        newUserDto = new NewUserDto();
        newUserDto.setUserName("newUser");
        newUserDto.setPassword("password123");

        userRole = new Role(1, RoleList.ROLE_USER);
    }

    @Test
    @DisplayName("authenticate: Debe autenticar al usuario y devolver un token JWT")
    void whenAuthenticate_thenReturnsJwtToken() {
        // GIVEN
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("dummy.jwt.token");

        // WHEN
        String token = authService.authenticate("testUser", "password");

        // THEN
        assertThat(token).isEqualTo("dummy.jwt.token");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(authentication);
    }

    @Test
    @DisplayName("registerUser: Debe registrar un nuevo usuario correctamente")
    void whenRegisterUser_thenSavesUser() {
        // GIVEN
        when(userService.existsByUserName("newUser")).thenReturn(false);
        when(roleRepository.findByName(RoleList.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // WHEN
        authService.registerUser(newUserDto);

        // THEN
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUserName()).isEqualTo("newUser");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo(userRole);
    }

    @Test
    @DisplayName("registerUser: Debe lanzar una excepción si el nombre de usuario ya existe")
    void whenRegisterUserWithExistingUsername_thenThrowsException() {
        // GIVEN
        when(userService.existsByUserName("newUser")).thenReturn(true);

        // WHEN & THEN
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(newUserDto);
        });
        assertThat(thrown.getMessage()).isEqualTo("El nombre de usuario ya existe");
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: Debe lanzar una excepción si el rol por defecto no se encuentra")
    void whenRegisterUserWithMissingRole_thenThrowsException() {
        // GIVEN
        when(userService.existsByUserName("newUser")).thenReturn(false);
        when(roleRepository.findByName(RoleList.ROLE_USER)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> authService.registerUser(newUserDto));
    }
}