package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.LoteCreateRequest;
import com.deusto.coffeestack.dto.LoteResponse;
import com.deusto.coffeestack.service.LoteService;
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
 * REST controller for supply batch (lote) reception and traceability.
 *
 * <p>Access rules:
 * <ul>
 *   <li>{@code GET} endpoints – any authenticated user</li>
 *   <li>{@code POST /api/lotes} – EMPLEADO, PROPIETARIO or ROOT</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Recepción y trazabilidad de lotes de insumos")
@SecurityRequirement(name = "basicAuth")
public class LoteController {

    private final LoteService service;

    public LoteController(LoteService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO','PROPIETARIO','ROOT')")
    @Operation(summary = "Registrar la recepción de un nuevo lote de insumo")
    public ResponseEntity<LoteResponse> recibir(@Valid @RequestBody LoteCreateRequest request) {
        LoteResponse created = service.recibirLote(request);
        return ResponseEntity
                .created(URI.create("/api/lotes/" + created.getId()))
                .body(created);
    }

    @GetMapping("/insumo/{insumoId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todos los lotes de un insumo")
    public List<LoteResponse> listarPorInsumo(@PathVariable Long insumoId) {
        return service.listarPorInsumo(insumoId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener un lote por su ID")
    public LoteResponse obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }
}
