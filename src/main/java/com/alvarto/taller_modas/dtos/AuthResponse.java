package com.alvarto.taller_modas.dtos;

public class AuthResponse {
    private String jwt;
    private String user;

    // Constructor
    public AuthResponse(String jwt, String user) {
        this.jwt = jwt;
        this.user = user;
    }

    // Getters y Setters
    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
