package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.VentaRequest;
import com.deusto.coffeestack.dto.VentaResponse;
import com.deusto.coffeestack.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller para el registro y consulta de ventas.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/ventas}       – registrar venta (EMPLEADO, PROPIETARIO, ROOT)</li>
 *   <li>{@code GET  /api/ventas}       – listar todas las ventas (cualquier autenticado)</li>
 *   <li>{@code GET  /api/ventas/{id}}  – detalle de una venta (cualquier autenticado)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Registro y consulta de ventas con descuento automático de inventario")
@SecurityRequirement(name = "basicAuth")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Registra una nueva venta, descuenta el inventario y genera los
     * movimientos de trazabilidad correspondientes.
     * Devuelve 201 Created con la venta en el body.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'PROPIETARIO', 'ROOT')")
    @Operation(summary = "Registrar una nueva venta y descontar inventario")
    public ResponseEntity<VentaResponse> registrar(
            @Valid @RequestBody VentaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuario = userDetails != null ? userDetails.getUsername() : "sistema";
        VentaResponse response = ventaService.registrarVenta(request, usuario);
        return ResponseEntity
                .created(URI.create("/api/ventas/" + response.getId()))
                .body(response);
    }

    /** Devuelve todas las ventas registradas, de más reciente a más antigua. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todas las ventas")
    public List<VentaResponse> listar() {
        return ventaService.listarVentas();
    }

    /** Devuelve el detalle completo de una venta por su id. */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener detalle de una venta")
    public VentaResponse obtener(@PathVariable Long id) {
        return ventaService.obtenerVenta(id);
    }
}
