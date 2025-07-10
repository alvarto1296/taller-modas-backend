package com.alvarto.taller_modas.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @jakarta.validation.constraints.NotBlank
    @Column(unique = true, nullable = false)
    private String userName;

    //@jakarta.validation.constraints.NotBlank
    @Column(nullable = false)
    private String password;

    @Column(unique = true) // El email del usuario de OAuth
    private String email;

    private String provider; // Ej. "GOOGLE", "FACEBOOK", "LOCAL"
    private boolean enabled = true; // Para controlar si el usuario está habilitado o no

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public User(String userName, String password, Role role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = userName; 
        this.provider = "LOCAL";
    }
}