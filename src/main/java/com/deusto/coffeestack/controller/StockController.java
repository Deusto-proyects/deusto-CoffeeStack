package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.CoberturaInsumoResponse;
import com.deusto.coffeestack.dto.StockInsumoResponse;
import com.deusto.coffeestack.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for stock consultation.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /api/stock/insumos}       – stock summary for all insumos</li>
 *   <li>{@code GET /api/stock/insumos/{id}}  – stock detail (with lote breakdown) for one insumo</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/stock")
@Tag(name = "Stock", description = "Consulta de stock por insumo")
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /** Returns stock summary (total quantity + shortage flag + lote detail) for every insumo. */
    @GetMapping("/insumos")
    public List<StockInsumoResponse> getAllStock() {
        return stockService.getStockTodosInsumos();
    }

    /** Returns stock detail for a single insumo. Returns 404 if the insumo does not exist. */
    @GetMapping("/insumos/{id}")
    public StockInsumoResponse getStockByInsumo(@PathVariable Long id) {
        return stockService.getStockDetalladoPorInsumo(id);
    }

    /**
     * Returns estimated coverage days per insumo, sorted from most critical to safest.
     *
     * <p>Coverage days = total stock / average daily consumption over the given window.
     * Restricted to PROPIETARIO and ROOT roles.
     */
    @GetMapping("/cobertura")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ROOT')")
    @Operation(
        summary = "Días de cobertura estimados por insumo",
        description = "Calcula cuántos días de stock quedan por insumo usando el consumo " +
                      "medio diario de la ventana indicada. Ordenado de más crítico a más seguro."
    )
    public List<CoberturaInsumoResponse> getCobertura(
            @Parameter(description = "Ventana de muestreo en días para calcular el consumo medio")
            @RequestParam(defaultValue = "30") int ventana) {
        return stockService.getCoberturaTodosInsumos(ventana);
    }
}
