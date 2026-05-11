package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;

import java.time.LocalDateTime;

public class UsuarioResponse {

    private Long id;
    private String username;
    private RolEnum rol;
    private boolean activo;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public static UsuarioResponse from(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.id = u.getId();
        r.username = u.getUsername();
        r.rol = u.getRol();
        r.activo = u.isActivo();
        r.createdAt = u.getCreatedAt();
        r.createdBy = u.getCreatedBy();
        r.updatedAt = u.getUpdatedAt();
        r.updatedBy = u.getUpdatedBy();
        return r;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public RolEnum getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
