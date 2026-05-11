package com.deusto.coffeestack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para editar datos de un usuario existente (sin cambiar rol ni activo).
 *
 * <p>{@code password} es opcional: si viene en blanco/null no se re-hashea.
 * Si se proporciona debe cumplir con la longitud mínima.
 */
public class UsuarioUpdateRequest {

    @NotBlank
    @Size(min = 3, max = 60)
    private String username;

    /** Opcional: si está presente se re-hashea con el PasswordEncoder. */
    @Size(min = 6, max = 100)
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
