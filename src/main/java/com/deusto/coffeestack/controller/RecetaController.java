package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.RecetaRequest;
import com.deusto.coffeestack.dto.RecetaResponse;
import com.deusto.coffeestack.service.RecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items/{itemId}/receta")
@Tag(name = "Recetas", description = "Gestión de recetas de productos (insumos y cantidades por venta)")
@SecurityRequirement(name = "bearerAuth")
public class RecetaController {

    private final RecetaService service;

    public RecetaController(RecetaService service) {
        this.service = service;
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(summary = "Definir o reemplazar la receta completa de un producto")
    public RecetaResponse definirReceta(@PathVariable Long itemId,
                                        @Valid @RequestBody RecetaRequest request) {
        return service.definirReceta(itemId, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar la receta de un producto")
    public RecetaResponse obtenerReceta(@PathVariable Long itemId) {
        return service.obtenerReceta(itemId);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(summary = "Eliminar la receta de un producto")
    public ResponseEntity<Void> eliminarReceta(@PathVariable Long itemId) {
        service.eliminarReceta(itemId);
        return ResponseEntity.noContent().build();
    }
}
