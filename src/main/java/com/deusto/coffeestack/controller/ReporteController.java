package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.ReporteComparativoResponse;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import com.deusto.coffeestack.dto.ReporteMotivoResponse;
import com.deusto.coffeestack.service.AjusteService;
import com.deusto.coffeestack.service.CsvExportService;
import com.deusto.coffeestack.service.ReporteComparativoService;
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
import java.time.LocalTime;
import java.util.List;

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
    private final ReporteComparativoService comparativoService;
    private final AjusteService ajusteService;

    public ReporteController(ReporteConsumoService service, CsvExportService csvExportService,
                             ReporteComparativoService comparativoService,
                             AjusteService ajusteService) {
        this.service = service;
        this.csvExportService = csvExportService;
        this.comparativoService = comparativoService;
        this.ajusteService = ajusteService;
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

    /**
     * Reporte comparativo de consumo para múltiples insumos en paralelo.
     *
     * <p>Permite al propietario identificar patrones y costes operativos
     * comparando el consumo de varios insumos en el mismo rango de fechas.
     *
     * @param insumoIds    IDs de los insumos a comparar (opcional; vacío = todos los activos)
     * @param desde        inicio del rango (inclusive)
     * @param hasta        fin del rango (inclusive)
     * @param granularidad agrupación temporal de la serie (DIA / SEMANA / MES)
     */
    @GetMapping("/consumo/comparativa")
    @Operation(summary = "Reporte comparativo de consumo para múltiples insumos")
    public ReporteComparativoResponse consumoComparativo(
            @RequestParam(required = false) List<Long> insumoIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIA") Granularidad granularidad) {
        return comparativoService.generar(insumoIds, desde, hasta, granularidad);
    }

    /**
     * Descarga el reporte de mermas/ajustes agrupado por motivo y tipo como CSV.
     *
     * <p>Permite al propietario exportar a Excel los patrones de desperdicio detectados
     * para análisis offline o elaboración de informes (issue #24).
     *
     * <p>Todos los parámetros son opcionales:
     * <ul>
     *   <li>{@code tipo}  – restringe a un tipo concreto (MERMA, ROTURA, …).</li>
     *   <li>{@code desde} – fecha inicial (inclusive). Se interpreta a las 00:00.</li>
     *   <li>{@code hasta} – fecha final (inclusive). Se interpreta al final del día.</li>
     * </ul>
     */
    @GetMapping("/motivos/csv")
    @Operation(summary = "Descargar reporte de mermas/ajustes por motivo en formato CSV")
    public ResponseEntity<byte[]> descargarMotivosCsv(
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        java.time.LocalDateTime desdeDt = desde == null ? null : desde.atStartOfDay();
        java.time.LocalDateTime hastaDt = hasta == null ? null : hasta.atTime(LocalTime.MAX);

        List<ReporteMotivoResponse> filas = ajusteService.reportePorMotivo(tipo, desdeDt, hastaDt);
        String csv = csvExportService.motivosToCsv(filas);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        String filename = "reporte_motivos_" + java.time.LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
