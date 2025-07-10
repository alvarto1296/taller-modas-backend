package com.alvarto.taller_modas.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alvarto.taller_modas.models.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
    Optional<User> findByEmail(String email);

}
