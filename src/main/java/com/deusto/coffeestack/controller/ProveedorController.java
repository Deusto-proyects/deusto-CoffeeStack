package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.ProveedorCreateRequest;
import com.deusto.coffeestack.dto.ProveedorResponse;
import com.deusto.coffeestack.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Gestion de proveedores para asociar compras")
@SecurityRequirement(name = "basicAuth")
public class ProveedorController {

    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar proveedores")
    public Page<ProveedorResponse> listar(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener proveedor por ID")
    public ProveedorResponse obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO','PROPIETARIO','ROOT')")
    @Operation(summary = "Registrar un proveedor")
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorCreateRequest request) {
        ProveedorResponse created = service.crear(request);
        return ResponseEntity
                .created(URI.create("/api/proveedores/" + created.getId()))
                .body(created);
    }
}
