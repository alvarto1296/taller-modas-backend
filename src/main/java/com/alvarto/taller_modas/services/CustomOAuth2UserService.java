// src/main/java/com/alvarto/taller_modas/services/CustomOAuth2UserService.java

package com.alvarto.taller_modas.services;

import com.alvarto.taller_modas.Enums.RoleList;
import com.alvarto.taller_modas.models.Role;
import com.alvarto.taller_modas.models.User;
import com.alvarto.taller_modas.repositories.RoleRepository;
import com.alvarto.taller_modas.repositories.UserRepository;
import com.alvarto.taller_modas.config.CustomOAuth2UserPrincipal; 
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        logger.info("CustomOAuth2UserService.loadUser() ha sido llamado.");

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getName();

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // user.setUserName(name); 
            userRepository.save(user);
        } else {
            user = new User();
            user.setEmail(email);
            user.setUserName(name); 
            user.setPassword(""); // OAuth sin password, modificar modelo
            user.setProvider("GOOGLE");

            Role defaultRole = roleRepository.findByName(RoleList.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Default role not found: " + RoleList.ROLE_USER));
            user.setRole(defaultRole);
            user.setEnabled(true);

            userRepository.save(user);
        }

        
        // Devolver una instancia de CustomOAuth2UserPrincipal, que envuelve User y los atributos de OAuth2
        return new CustomOAuth2UserPrincipal(user, oAuth2User);
    }
}