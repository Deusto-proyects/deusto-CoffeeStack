package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.AjusteRequest;
import com.deusto.coffeestack.dto.MovimientoResponse;
import com.deusto.coffeestack.dto.ReporteMotivoResponse;
import com.deusto.coffeestack.service.AjusteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * REST controller for registering inventory adjustments (mermas, roturas, manual adjustments).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/ajustes}                      – register a new movement (PROPIETARIO/ROOT)</li>
 *   <li>{@code GET  /api/ajustes}                      – list all movements (any authenticated user)</li>
 *   <li>{@code GET  /api/ajustes/insumo/{insumoId}}    – list movements for a specific insumo</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ajustes")
@Tag(name = "Ajustes de inventario", description = "Registro de mermas, roturas y ajustes manuales")
@SecurityRequirement(name = "basicAuth")
public class AjusteController {

    private final AjusteService ajusteService;

    public AjusteController(AjusteService ajusteService) {
        this.ajusteService = ajusteService;
    }

    /**
     * Registers a new inventory movement.
     * Returns 201 Created with the movement in the body and its URI in the Location header.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ROOT')")
    @Operation(summary = "Registrar una merma, rotura o ajuste de inventario")
    public ResponseEntity<MovimientoResponse> registrar(
            @Valid @RequestBody AjusteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuario = userDetails != null ? userDetails.getUsername() : "sistema";
        MovimientoResponse response = ajusteService.registrarAjuste(request, usuario);
        return ResponseEntity
                .created(URI.create("/api/ajustes/" + response.getId()))
                .body(response);
    }

    /** Returns the full list of inventory movements (audit log), most recent first. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todos los movimientos de inventario")
    public List<MovimientoResponse> listar() {
        return ajusteService.listarMovimientos();
    }

    /**
     * Returns all movements for the batches of a given insumo.
     * Returns 404 if the insumo does not exist.
     */
    @GetMapping("/insumo/{insumoId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar movimientos por insumo")
    public List<MovimientoResponse> listarPorInsumo(@PathVariable Long insumoId) {
        return ajusteService.listarMovimientosPorInsumo(insumoId);
    }

    /**
     * Reporte agregado de mermas/ajustes agrupados por motivo y tipo. Pensado
     * para que el propietario detecte recurrencias de desperdicio y mejore
     * procesos (issue #24).
     *
     * <p>Todos los parámetros son opcionales:
     * <ul>
     *   <li>{@code tipo}   – filtra por un tipo concreto (MERMA, ROTURA, …).</li>
     *   <li>{@code desde}  – fecha inicial (inclusive). Se interpreta a las 00:00.</li>
     *   <li>{@code hasta}  – fecha final (inclusive). Se interpreta al final del día.</li>
     * </ul>
     */
    @GetMapping("/reporte-motivos")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ROOT')")
    @Operation(summary = "Reporte de movimientos agrupados por motivo y tipo")
    public List<ReporteMotivoResponse> reportePorMotivo(
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDateTime desdeDt = desde == null ? null : desde.atStartOfDay();
        LocalDateTime hastaDt = hasta == null ? null : hasta.atTime(LocalTime.MAX);
        return ajusteService.reportePorMotivo(tipo, desdeDt, hastaDt);
    }
}
