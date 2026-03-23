package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.RolEnum;
import jakarta.validation.constraints.NotNull;

public class CambiarRolRequest {

    @NotNull
    private RolEnum rol;

    public RolEnum getRol() { return rol; }
    public void setRol(RolEnum rol) { this.rol = rol; }
}
