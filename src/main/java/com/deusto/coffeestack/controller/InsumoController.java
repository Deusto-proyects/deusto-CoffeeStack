package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.InsumoCreateRequest;
import com.deusto.coffeestack.dto.InsumoResponse;
import com.deusto.coffeestack.dto.InsumoUpdateRequest;
import com.deusto.coffeestack.service.InsumoService;
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

/**
 * REST controller for insumo (supply/ingredient) catalogue management.
 *
 * <p>Access rules:
 * <ul>
 *   <li>{@code GET}  endpoints – any authenticated user (EMPLEADO, PROPIETARIO, ROOT)</li>
 *   <li>{@code POST / PUT} – PROPIETARIO or ROOT (managers create/edit supplies)</li>
 *   <li>{@code DELETE /{id}} – PROPIETARIO or ROOT (soft-deactivation)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/insumos")
@Tag(name = "Insumos", description = "Gestión del catálogo de insumos")
@SecurityRequirement(name = "basicAuth")
public class InsumoController {

    private final InsumoService service;

    public InsumoController(InsumoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar insumos (paginado)")
    public Page<InsumoResponse> listar(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener insumo por ID")
    public InsumoResponse obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(summary = "Crear un nuevo insumo")
    public ResponseEntity<InsumoResponse> crear(@Valid @RequestBody InsumoCreateRequest request) {
        InsumoResponse created = service.crear(request);
        return ResponseEntity
                .created(URI.create("/api/insumos/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(summary = "Editar un insumo existente")
    public InsumoResponse actualizar(@PathVariable Long id,
                                     @Valid @RequestBody InsumoUpdateRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(summary = "Desactivar un insumo (baja lógica)")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
