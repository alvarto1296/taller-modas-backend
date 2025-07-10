// src/main/java/com/alvarto/taller_modas.config/CustomOAuth2UserPrincipal.java
package com.alvarto.taller_modas.config;

import com.alvarto.taller_modas.models.User; // Importa tu entidad User
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomOAuth2UserPrincipal implements OAuth2User, UserDetails {

    private final User user;
    private final OAuth2User oAuth2User;
    private final Collection<? extends GrantedAuthority> authorities; 

    public CustomOAuth2UserPrincipal(User user, OAuth2User oAuth2User) {
        this.user = user;
        this.oAuth2User = oAuth2User;
        // Mapea el Set<Role> a una Collection de GrantedAuthority
        if (user != null && user.getRole() != null) {
             this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getName().name())
            );
        } else {
            // Si el usuario no tiene roles asignar rol
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    // --- Implementación de OAuth2User ---
    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public String getName() {
        return oAuth2User.getName();
    }

    // --- Implementación de UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Devuelve la colección de autoridades inicializada
    }

    @Override
    public String getPassword() {
        
        return user != null && user.getPassword() != null ? user.getPassword() : "";
    }

    @Override
    public String getUsername() {
        
        return user != null && user.getEmail() != null ? user.getEmail() : oAuth2User.getAttribute("email");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.isEnabled(); 
    }

    public User getUser() {
        return user;
    }
}