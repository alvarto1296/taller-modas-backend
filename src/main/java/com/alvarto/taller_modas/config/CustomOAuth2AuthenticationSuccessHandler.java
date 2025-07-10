package com.alvarto.taller_modas.config;

import com.alvarto.taller_modas.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

//@Component
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final String frontendRedirectUrl;

    public CustomOAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, String frontendRedirectUrl) {
        this.jwtUtil = jwtUtil;
        this.frontendRedirectUrl = frontendRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Ahora, 'authentication.getPrincipal()' SIEMPRE será un UserDetails
        // ya sea tu CustomOAuth2UserPrincipal (para OAuth) o el UserDetails de tu UserService (para autenticación local).
        // El casteo a UserDetails es ahora seguro.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Genera el JWT usando el Authentication, que ahora contiene un principal que es un UserDetails.
        // Tu método JwtUtil.generateToken(Authentication authentication) debería funcionar sin problemas.
        String token = jwtUtil.generateToken(authentication);

        // Construye la URL de redirección para el frontend, incluyendo el token
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("token", token) // Pasar el token como parámetro de query
                .queryParam("user", userDetails.getUsername()) // Puedes pasar más información si es necesario
                .build().toUriString();

        // Realiza la redirección
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}