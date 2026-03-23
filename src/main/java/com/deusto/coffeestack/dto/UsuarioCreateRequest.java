package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.RolEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioCreateRequest {

    @NotBlank
    @Size(min = 3, max = 60)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotNull
    private RolEnum rol;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RolEnum getRol() { return rol; }
    public void setRol(RolEnum rol) { this.rol = rol; }
}
