package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.SugerenciaReposicionResponse;
import com.deusto.coffeestack.service.ReposicionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints para consultar sugerencias de reposición por insumo.
 */
@RestController
@RequestMapping("/api/insumos")
@Tag(name = "Reposición", description = "Sugerencias de cantidad a comprar por insumo")
@SecurityRequirement(name = "bearerAuth")
public class ReposicionController {

    private final ReposicionService reposicionService;

    public ReposicionController(ReposicionService reposicionService) {
        this.reposicionService = reposicionService;
    }

    @GetMapping("/sugerencias-reposicion")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(summary = "Sugerencias de reposición para todos los insumos activos",
               description = "Devuelve, por cada insumo activo, la cantidad sugerida de compra y el " +
                             "nivel de urgencia (OK / ATENCION / URGENTE) calculados a partir del stock " +
                             "actual, el consumo medio diario y los parámetros del insumo (lead time y " +
                             "días de cobertura).")
    public List<SugerenciaReposicionResponse> listar(
            @Parameter(description = "Ventana en días para estimar el consumo medio")
            @RequestParam(defaultValue = "30") int ventana) {
        return reposicionService.calcularSugerencias(ventana);
    }

    @GetMapping("/{id}/sugerencia-reposicion")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(summary = "Sugerencia de reposición para un insumo concreto")
    public ResponseEntity<SugerenciaReposicionResponse> detalle(
            @PathVariable("id") Long id,
            @Parameter(description = "Ventana en días para estimar el consumo medio")
            @RequestParam(defaultValue = "30") int ventana) {
        return ResponseEntity.ok(reposicionService.calcularSugerenciaPorInsumo(id, ventana));
    }
}
