package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.MovimientoResponse;
import com.deusto.coffeestack.service.AjusteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * REST controller for querying the full inventory movement history (audit log).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /api/movimientos} – filtered history (PROPIETARIO/ROOT only)</li>
 * </ul>
 *
 * <p>All query parameters are optional. When omitted the filter is not applied.
 */
@RestController
@RequestMapping("/api/movimientos")
@Tag(name = "Historial de movimientos", description = "Consulta de auditoría del historial de inventario")
@SecurityRequirement(name = "basicAuth")
public class MovimientoController {

    private final AjusteService ajusteService;

    public MovimientoController(AjusteService ajusteService) {
        this.ajusteService = ajusteService;
    }

    /**
     * Returns the inventory movement history with optional filters.
     *
     * <p>Query params (all optional):
     * <ul>
     *   <li>{@code insumoId} – filter by insumo</li>
     *   <li>{@code tipo}     – filter by movement type (MERMA, ROTURA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, VENTA)</li>
     *   <li>{@code desde}    – start date (yyyy-MM-dd), inclusive; time defaults to 00:00:00</li>
     *   <li>{@code hasta}    – end date   (yyyy-MM-dd), inclusive; time defaults to 23:59:59</li>
     * </ul>
     * Results are always ordered most-recent first.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ROOT')")
    @Operation(summary = "Consultar historial de movimientos de inventario con filtros opcionales")
    public List<MovimientoResponse> historial(
            @Parameter(description = "ID del insumo (opcional)")
            @RequestParam(required = false) Long insumoId,

            @Parameter(description = "Tipo de movimiento: MERMA, ROTURA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, VENTA (opcional)")
            @RequestParam(required = false) TipoMovimiento tipo,

            @Parameter(description = "Fecha inicio (yyyy-MM-dd), inclusive (opcional)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,

            @Parameter(description = "Fecha fin (yyyy-MM-dd), inclusive (opcional)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        LocalDateTime desdeDateTime = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = (hasta != null) ? hasta.atTime(LocalTime.MAX) : null;

        return ajusteService.listarMovimientosFiltrados(insumoId, tipo, desdeDateTime, hastaDateTime);
    }
}
