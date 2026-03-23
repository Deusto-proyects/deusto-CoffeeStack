package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.CambiarRolRequest;
import com.deusto.coffeestack.dto.UsuarioCreateRequest;
import com.deusto.coffeestack.dto.UsuarioResponse;
import com.deusto.coffeestack.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for user and role management.
 *
 * <p>All endpoints require the ROOT role.
 *
 * <ul>
 *   <li>{@code POST   /api/usuarios}           – create user</li>
 *   <li>{@code GET    /api/usuarios}           – list users</li>
 *   <li>{@code PATCH  /api/usuarios/{id}/rol}  – change role</li>
 *   <li>{@code DELETE /api/usuarios/{id}}      – deactivate user</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ROOT')")
@Tag(name = "Usuarios", description = "Gestión de usuarios y roles (solo ROOT)")
@SecurityRequirement(name = "basicAuth")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear usuario")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse created = service.crear(request);
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + created.getId()))
                .body(created);
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar el rol de un usuario")
    public UsuarioResponse cambiarRol(@PathVariable Long id,
                                      @Valid @RequestBody CambiarRolRequest request) {
        return service.cambiarRol(id, request.getRol());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar un usuario")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
