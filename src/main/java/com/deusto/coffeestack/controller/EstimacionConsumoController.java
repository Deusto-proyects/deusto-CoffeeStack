package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.EstimacionConsumoResponse;
import com.deusto.coffeestack.service.EstimacionConsumoService;
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

/**
 * Endpoint para consultar la estimación de consumo diario futuro de un insumo.
 */
@RestController
@RequestMapping("/api/insumos")
@Tag(name = "Estimación de consumo", description = "Estimación de consumo diario futuro por insumo")
@SecurityRequirement(name = "bearerAuth")
public class EstimacionConsumoController {

    private final EstimacionConsumoService estimacionConsumoService;

    public EstimacionConsumoController(EstimacionConsumoService estimacionConsumoService) {
        this.estimacionConsumoService = estimacionConsumoService;
    }

    @GetMapping("/{id}/estimacion-consumo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Estimar el consumo diario futuro de un insumo",
               description = "Calcula el consumo medio diario de un insumo a partir de los movimientos " +
                             "que reducen stock (ventas, mermas, roturas y ajustes negativos) en la ventana " +
                             "indicada y proyecta ese ritmo al horizonte solicitado.")
    public ResponseEntity<EstimacionConsumoResponse> estimar(
            @PathVariable("id") Long id,
            @Parameter(description = "Tamaño de la ventana de muestreo en días")
            @RequestParam(defaultValue = "30") int ventana,
            @Parameter(description = "Horizonte en días para la proyección")
            @RequestParam(defaultValue = "7") int horizonte) {
        return ResponseEntity.ok(estimacionConsumoService.calcular(id, ventana, horizonte));
    }
}
