package com.alvarto.taller_modas.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.alvarto.taller_modas.Enums.RoleList;
import com.alvarto.taller_modas.dtos.NewUserDto;
import com.alvarto.taller_modas.jwt.JwtUtil;
import com.alvarto.taller_modas.models.Role;
import com.alvarto.taller_modas.models.User;
import com.alvarto.taller_modas.repositories.RoleRepository;

@Service
public class AuthService {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    
    public AuthService(UserService userService, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    public String authenticate(String username, String password){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,password);
        Authentication authResult = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        return jwtUtil.generateToken(authResult);
    }

    public void registerUser(NewUserDto newUserDto){
        if (userService.existsByUserName(newUserDto.getUserName())){
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        Role roleUser = roleRepository.findByName(RoleList.ROLE_USER).orElseThrow(()->new RuntimeException("Rol no encontrado"));
        User user = new User(newUserDto.getUserName(), passwordEncoder.encode(newUserDto.getPassword()) , roleUser);
        userService.save(user);
    }
}