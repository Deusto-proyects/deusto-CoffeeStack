package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import com.deusto.coffeestack.service.ReporteConsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Reportes operacionales para el propietario del negocio.
 *
 * <p>Solo accesibles para los roles PROPIETARIO y ROOT.
 */
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAnyRole('PROPIETARIO', 'ROOT')")
@Tag(name = "Reportes", description = "Reportes operacionales (PROPIETARIO/ROOT)")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteConsumoService service;

    public ReporteController(ReporteConsumoService service) {
        this.service = service;
    }

    @GetMapping("/consumo")
    @Operation(summary = "Reporte de consumo por insumo en un rango de fechas")
    public ReporteConsumoResponse consumoPorInsumo(
            @RequestParam Long insumoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIA") Granularidad granularidad) {
        return service.generar(insumoId, desde, hasta, granularidad);
    }
}
