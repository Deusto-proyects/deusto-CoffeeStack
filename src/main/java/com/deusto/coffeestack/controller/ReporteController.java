package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import com.deusto.coffeestack.service.CsvExportService;
import com.deusto.coffeestack.service.ReporteConsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
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
    private final CsvExportService csvExportService;

    public ReporteController(ReporteConsumoService service, CsvExportService csvExportService) {
        this.service = service;
        this.csvExportService = csvExportService;
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

    /**
     * Descarga el reporte de consumo de insumos como fichero CSV.
     * Incluye metadatos de cabecera y la serie temporal completa.
     * El fichero incluye BOM UTF-8 para compatibilidad con Microsoft Excel.
     */
    @GetMapping("/consumo/csv")
    @Operation(summary = "Descargar reporte de consumo en formato CSV")
    public ResponseEntity<byte[]> descargarConsumoCsv(
            @RequestParam Long insumoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIA") Granularidad granularidad) {
        ReporteConsumoResponse reporte = service.generar(insumoId, desde, hasta, granularidad);
        String csv = csvExportService.consumoToCsv(reporte);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        String filename = "consumo_" + reporte.getInsumoNombre().replaceAll("\\s+", "_") + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
