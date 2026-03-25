package com.deusto.coffeestack.dto;

public class AuthResponse {

    private final String token;
    private final String role;
    private final String username;

    public AuthResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
}
