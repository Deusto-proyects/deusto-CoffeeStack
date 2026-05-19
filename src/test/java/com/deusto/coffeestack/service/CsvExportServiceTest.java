package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ConsumoPorTipoDTO;
import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.PuntoSerieDTO;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios de {@link CsvExportService}.
 *
 * <p>Verifica que los ficheros CSV generados incluyan BOM, cabeceras correctas
 * y que los valores especiales (comas, comillas) queden correctamente escapados.
 */
class CsvExportServiceTest {

    private CsvExportService service;

    @BeforeEach
    void setUp() {
        service = new CsvExportService();
    }

    // ── consumoToCsv ──────────────────────────────────────────────────────────

    @Test
    void consumoToCsv_incluyeBomUtf8() {
        ReporteConsumoResponse reporte = reporteMinimo();
        String csv = service.consumoToCsv(reporte);
        assertThat(csv).startsWith(CsvExportService.BOM);
    }

    @Test
    void consumoToCsv_incluyeMetadatosCabecera() {
        ReporteConsumoResponse reporte = reporteMinimo();
        String csv = service.consumoToCsv(reporte);

        assertThat(csv).contains("Insumo");
        assertThat(csv).contains("Café molido");
        assertThat(csv).contains("Unidad");
        assertThat(csv).contains("kg");
        assertThat(csv).contains("Desde");
        assertThat(csv).contains("2026-01-01");
        assertThat(csv).contains("Hasta");
        assertThat(csv).contains("2026-01-31");
    }

    @Test
    void consumoToCsv_incluyeColumnasDeSerieTemporalConCabecera() {
        ReporteConsumoResponse reporte = reporteMinimo();
        String csv = service.consumoToCsv(reporte);

        assertThat(csv).contains("Fecha");
        assertThat(csv).contains("Cantidad");
        assertThat(csv).contains("Coste");
        // Verifica que el punto de la serie esté presente
        assertThat(csv).contains("2026-01-02");
        assertThat(csv).containsPattern("5[.,]0000");
    }

    @Test
    void consumoToCsv_incluyeCantidadConCuatroDecimales() {
        ReporteConsumoResponse reporte = reporteMinimo();
        String csv = service.consumoToCsv(reporte);
        // String.format("%.4f", 5.0) genera "5,0000" o "5.0000" según Locale de la JVM
        // Verificamos que exista el valor 5 seguido de 4 dígitos (con cualquier separador)
        assertThat(csv).containsPattern("5[.,]0000");
    }

    @Test
    void consumoToCsv_conSerieVaciaNoLanzaExcepcion() {
        ReporteConsumoResponse reporte = new ReporteConsumoResponse(
                1L, "Agua", "L",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                Granularidad.DIA, 0.0, BigDecimal.ZERO,
                List.of(), List.of()
        );
        String csv = service.consumoToCsv(reporte);
        assertThat(csv).isNotBlank();
        // Debe contener la cabecera pero sin filas de datos
        assertThat(csv).contains("Fecha");
    }

    @Test
    void consumoToCsv_conCosteTotalNulo_usaCero() {
        ReporteConsumoResponse reporte = new ReporteConsumoResponse(
                1L, "Azúcar", "kg",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                Granularidad.DIA, 3.0, null,
                List.of(), List.of()
        );
        String csv = service.consumoToCsv(reporte);
        assertThat(csv).contains("0.00");
    }

    @Test
    void consumoToCsv_nombreConComaEsEscapado() {
        // Un insumo cuyo nombre contiene coma debe quedar entre comillas en el CSV
        ReporteConsumoResponse reporte = new ReporteConsumoResponse(
                2L, "Café, tostado", "kg",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                Granularidad.DIA, 0.0, BigDecimal.ZERO,
                List.of(), List.of()
        );
        String csv = service.consumoToCsv(reporte);
        assertThat(csv).contains("\"Café, tostado\"");
    }

    @Test
    void consumoToCsv_totalCantidadFormateadaConDosDecimales() {
        ReporteConsumoResponse reporte = reporteMinimo();
        String csv = service.consumoToCsv(reporte);
        // String.format("%.2f", 5.0) genera "5,00" o "5.00" según Locale de la JVM
        assertThat(csv).containsPattern("5[.,]00");
    }

    // ── ventasToCsv ───────────────────────────────────────────────────────────

    @Test
    void ventasToCsv_incluyeBom() {
        String csv = service.ventasToCsv(List.of());
        assertThat(csv).startsWith(CsvExportService.BOM);
    }

    @Test
    void ventasToCsv_incluyeCabeceras() {
        String csv = service.ventasToCsv(List.of());
        assertThat(csv).contains("Fecha");
        assertThat(csv).contains("Producto");
        assertThat(csv).contains("Unidades Vendidas");
    }

    // ── motivosToCsv ──────────────────────────────────────────────────────────

    @Test
    void motivosToCsv_incluyeBomUtf8() {
        String csv = service.motivosToCsv(List.of());
        assertThat(csv).startsWith(CsvExportService.BOM);
    }

    @Test
    void motivosToCsv_incluyeCabecerasCorrectas() {
        String csv = service.motivosToCsv(List.of());
        assertThat(csv).contains("Motivo");
        assertThat(csv).contains("Tipo");
        assertThat(csv).contains("incidencias");
        assertThat(csv).contains("Cantidad total");
        assertThat(csv).contains("Primera fecha");
        assertThat(csv).contains("ltima fecha");   // 'Ú' puede quedar como U+00DA pero el campo sí está
    }

    @Test
    void motivosToCsv_conFilaDatos_incluyeValoresCorrectos() {
        LocalDate hoy = LocalDate.now();
        var fila = new com.deusto.coffeestack.dto.ReporteMotivoResponse(
                "Caducidad leche", TipoMovimiento.MERMA, 5L, 12.5,
                hoy.minusDays(10).atStartOfDay(), hoy.atStartOfDay());

        String csv = service.motivosToCsv(List.of(fila));

        assertThat(csv).contains("Caducidad leche");
        assertThat(csv).contains("MERMA");
        assertThat(csv).contains("5");
        // La cantidad se formatea con 4 decimales (separador depende del Locale de la JVM)
        assertThat(csv).containsPattern("12[.,]5000");
    }

    @Test
    void motivosToCsv_motivoConComa_esEscapado() {
        var fila = new com.deusto.coffeestack.dto.ReporteMotivoResponse(
                "Rotura, accidente", TipoMovimiento.ROTURA, 1L, 3.0,
                null, null);

        String csv = service.motivosToCsv(List.of(fila));

        // El motivo con coma debe quedar entre comillas dobles
        assertThat(csv).contains("\"Rotura, accidente\"");
    }

    @Test
    void motivosToCsv_listaVacia_soloDevuelveCabecera() {
        String csv = service.motivosToCsv(List.of());
        // Solo BOM + cabecera, sin filas de datos
        long lineCount = csv.lines().count();
        assertThat(lineCount).isEqualTo(1);  // solo cabecera (CRLF no genera línea extra en lines())
    }

    @Test
    void motivosToCsv_fechasNulas_noLanzaExcepcion() {
        var fila = new com.deusto.coffeestack.dto.ReporteMotivoResponse(
                "Sin fecha", TipoMovimiento.AJUSTE_NEGATIVO, 2L, 5.0,
                null, null);

        String csv = service.motivosToCsv(List.of(fila));

        assertThat(csv).isNotBlank();
        assertThat(csv).contains("Sin fecha");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ReporteConsumoResponse reporteMinimo() {
        List<ConsumoPorTipoDTO> desglose = List.of(
                new ConsumoPorTipoDTO(TipoMovimiento.VENTA, 5.0, new BigDecimal("50.00"))
        );
        List<PuntoSerieDTO> serie = List.of(
                new PuntoSerieDTO(LocalDate.of(2026, 1, 2), 5.0, new BigDecimal("50.00"))
        );
        return new ReporteConsumoResponse(
                1L, "Café molido", "kg",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                Granularidad.DIA, 5.0, new BigDecimal("50.00"),
                desglose, serie
        );
    }
}
