package com.alvarto.taller_modas.config;

import com.alvarto.taller_modas.jwt.JwtAuthenticationFilter;
import com.alvarto.taller_modas.jwt.JwtEntryPoint;
import com.alvarto.taller_modas.jwt.JwtUtil; // Asegúrate de importar JwtUtil
import com.alvarto.taller_modas.services.CustomOAuth2UserService; // Importar tu servicio personalizado OAuth2
import com.alvarto.taller_modas.services.UserService; // Importar tu UserService
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // Importar AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Importar DaoAuthenticationProvider
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Importar AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // ¡Importante para @PreAuthorize!
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // Importar SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita la seguridad basada en métodos con @PreAuthorize
public class SecurityConfig {

    // Inyecta las dependencias necesarias
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService; // Tu servicio de usuarios OAuth2
    

    public SecurityConfig(UserService userService, JwtUtil jwtUtil, CustomOAuth2UserService customOAuth2UserService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public JwtAuthenticationFilter jwtTokenFilter() {
        // Necesitas que JwtAuthenticationFilter use las instancias inyectadas
        // Puedes pasarles el jwtUtil y userService en el constructor si no usas @Autowired directamente en el filtro
        return new JwtAuthenticationFilter(jwtUtil, userService);
    }

    @Bean
    public JwtEntryPoint jwtEntryPoint() {
        return new JwtEntryPoint();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración CORS
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080","http://localhost:3000", "http://localhost:4200")); 
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Bean para AuthenticationManager (necesario para AuthService)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Bean para el DaoAuthenticationProvider (para la autenticación local username/password)
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService); // Tu servicio para cargar usuarios locales
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
public String checkCustomOAuth2UserServiceBean() {
    // Esto se ejecutará cuando SecurityConfig se inicialice.
    // Si customOAuth2UserService es null aquí, significa que Spring no pudo inyectarlo.
    if (customOAuth2UserService != null) {
        System.out.println("DEBUG: customOAuth2UserService bean está disponible en SecurityConfig.");
    } else {
        System.out.println("ERROR: customOAuth2UserService bean NO está disponible en SecurityConfig.");
    }
    return "CustomOAuth2UserService Bean Check";
}

    // ** NUEVO: Bean para el manejador de éxito de autenticación OAuth2 **
    @Bean
    public CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler() {
        // Pasa jwtUtil al constructor para que pueda generar el token
        // y la URL de redirección de tu frontend
        return new CustomOAuth2AuthenticationSuccessHandler(jwtUtil, "http://localhost:3000");
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            // Configura la política de sesión: STATELESS para JWT, pero Spring Security
            // manejará sesiones para OAuth2 internamente para el flujo de redirección.
            // Para JWT, es crucial que no cree sesiones persistentes.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas que no requieren autenticación
                .requestMatchers(
                    "/auth/register",
                    "/auth/login",
                    "/oauth2/**", // Rutas para el flujo de OAuth2 (ej. /oauth2/authorization/google)
                    "/login/oauth2/code/**", // URI de redirección de OAuth2 (callback)
                    "/swagger-ui/**", // Si usas Swagger
                    "/v3/api-docs/**" // Si usas OpenAPI/Swagger
                ).permitAll()
                .anyRequest().authenticated() // Todas las demás rutas protegidas
            )
            .oauth2Login(oauth2 -> oauth2 // Habilita la autenticación OAuth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // <-- Usa tu CustomOAuth2UserService aquí
                )
                // Redirige al manejador personalizado para generar JWT y redirigir al frontend
                .successHandler(customOAuth2AuthenticationSuccessHandler())
                .failureUrl("/login?error=true") // Redirige aquí si falla la autenticación OAuth2
            )
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtEntryPoint()))
            .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // Si tu lógica de logout con JWT es simple (solo limpiar el contexto de seguridad),
        // no necesitas el .logout() explícito aquí si ya usas JWT para todo.
        // Si tienes sesiones de Spring Security involucradas (ej. HttpOnly cookies),
        // entonces el .logout() es útil. Para un enfoque puramente stateless con JWT en localStorage,
        // el logout se maneja principalmente en el frontend borrando el token.
        // Si decides mantenerlo para limpieza adicional (ej. cookies), déjalo.
        // .logout(logout -> logout
        //     .logoutSuccessUrl("/auth/login")
        //     .addLogoutHandler(new SecurityContextLogoutHandler())
        //     .deleteCookies("JSESSIONID")
        // )
        // .httpBasic(Customizer.withDefaults()); // Generalmente no se usa con JWT

        return http.build();
    }
}