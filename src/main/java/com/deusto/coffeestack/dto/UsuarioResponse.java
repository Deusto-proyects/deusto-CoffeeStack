package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;

public class UsuarioResponse {

    private Long id;
    private String username;
    private RolEnum rol;
    private boolean activo;

    public static UsuarioResponse from(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.id = u.getId();
        r.username = u.getUsername();
        r.rol = u.getRol();
        r.activo = u.isActivo();
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
}
