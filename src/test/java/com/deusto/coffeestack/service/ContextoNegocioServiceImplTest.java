package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.CoberturaInsumoResponse;
import com.deusto.coffeestack.dto.InsumoResponse;
import com.deusto.coffeestack.dto.LoteResponse;
import com.deusto.coffeestack.dto.ReporteVentasDTO;
import com.deusto.coffeestack.dto.StockInsumoResponse;
import com.deusto.coffeestack.dto.SugerenciaReposicionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContextoNegocioServiceImpl — tests unitarios")
class ContextoNegocioServiceImplTest {

    @Mock ReposicionService reposicionService;
    @Mock StockService stockService;
    @Mock VentaService ventaService;

    ContextoNegocioServiceImpl service;

    @BeforeEach
    void setUp() {
        // TTL=0 desactiva la caché para que cada test mida la generación real.
        service = new ContextoNegocioServiceImpl(reposicionService, stockService, ventaService,
                0L, Clock.systemUTC());
    }

    // ──────────────────────────────────────────────────────────────────────
    // Estructura general
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("snapshot incluye cabecera con la fecha actual")
    void snapshot_incluyeCabeceraFecha() {
        emptyDataset();
        String snapshot = service.generarSnapshot();
        assertThat(snapshot).startsWith("## Fecha del snapshot: " + LocalDate.now());
    }

    @Test
    @DisplayName("snapshot incluye todas las secciones esperadas")
    void snapshot_incluyeTodasLasSecciones() {
        emptyDataset();
        String snapshot = service.generarSnapshot();
        assertThat(snapshot).contains("## Inventario actual");
        assertThat(snapshot).contains("## Cobertura por insumo");
        assertThat(snapshot).contains("## Riesgo de merma por caducidad");
        assertThat(snapshot).contains("## Reposición urgente");
        assertThat(snapshot).contains("## Riesgo de faltantes");
        assertThat(snapshot).contains("## Cobertura crítica");
        assertThat(snapshot).contains("## Top productos");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Inventario actual (siempre presente con todos los insumos)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("inventario actual lista todos los insumos, incluso los sin riesgo")
    void inventarioActual_listaTodosLosInsumos() {
        InsumoResponse cafe = new InsumoResponse(1L, "Café", "kg", 2.0, true, 7, 14);
        InsumoResponse leche = new InsumoResponse(2L, "Leche", "L", 5.0, true, 7, 14);
        when(stockService.getStockTodosInsumos()).thenReturn(List.of(
                new StockInsumoResponse(cafe, 17.0, false, List.of()),
                new StockInsumoResponse(leche, 22.0, false, List.of())));
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String snapshot = service.generarSnapshot();
        String inv = section(snapshot, "## Inventario actual");

        assertThat(inv).contains("Café");
        assertThat(inv).contains("Leche");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Caducidades próximas
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("riesgo de merma: filtra lotes con vencimiento ≤ 30 días y ordena por merma probable descendente")
    void merma_filtraYOrdena() {
        LocalDate hoy = LocalDate.now();
        InsumoResponse leche = new InsumoResponse(1L, "Leche", "L", 5.0, true, 7, 14);
        InsumoResponse cafe = new InsumoResponse(2L, "Café", "kg", 2.0, true, 7, 14);

        LoteResponse lecheVencePronto = new LoteResponse(1L, "L-LECHE-001", 30.0, 22.0,
                hoy.plusDays(10), "Lácteos Norte", BigDecimal.valueOf(1.20));
        LoteResponse cafeVenceMedio = new LoteResponse(2L, "L-CAFE-001", 20.0, 17.0,
                hoy.plusDays(20), "Cafés del Mundo", BigDecimal.valueOf(18.50));
        LoteResponse vasosLejos = new LoteResponse(3L, "L-VASOS-001", 500.0, 380.0,
                hoy.plusDays(200), null, BigDecimal.valueOf(0.04));
        LoteResponse loteAgotado = new LoteResponse(4L, "L-CADUCO", 10.0, 0.0,
                hoy.plusDays(5), null, BigDecimal.ZERO);

        when(stockService.getStockTodosInsumos()).thenReturn(List.of(
                new StockInsumoResponse(leche, 22.0, false, List.of(lecheVencePronto, loteAgotado)),
                new StockInsumoResponse(cafe, 17.0, false, List.of(cafeVenceMedio, vasosLejos))));
        // Consumo: leche 0.2 L/día → 10 días = 2 L consumidos, merma probable 20 L.
        //          café  0.5 kg/día → 20 días = 10 kg consumidos, merma probable 7 kg.
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of(
                new CoberturaInsumoResponse(1L, "Leche", "L", 22.0, 0.2, 110.0, "OK", 30),
                new CoberturaInsumoResponse(2L, "Café", "kg", 17.0, 0.5, 34.0, "OK", 30)));
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String merma = section(service.generarSnapshot(), "## Riesgo de merma por caducidad");

        assertThat(merma).contains("L-LECHE-001");
        assertThat(merma).contains("L-CAFE-001");
        assertThat(merma).doesNotContain("L-VASOS-001");
        assertThat(merma).doesNotContain("L-CADUCO");
        // Orden por merma probable desc: leche (20 L) antes que café (7 kg).
        assertThat(merma.indexOf("L-LECHE-001")).isLessThan(merma.indexOf("L-CAFE-001"));
    }

    @Test
    @DisplayName("riesgo de merma: sin lotes cercanos → texto informativo")
    void merma_sinLotesCercanos_marca() {
        emptyDataset();
        String snapshot = service.generarSnapshot();
        String merma = section(snapshot, "## Riesgo de merma por caducidad");
        assertThat(merma).contains("no se prevén mermas");
    }

    @Test
    @DisplayName("riesgo de merma: sin consumo registrado, la merma probable iguala al stock")
    void merma_sinConsumo_mermaIgualAStock() {
        LocalDate hoy = LocalDate.now();
        InsumoResponse leche = new InsumoResponse(1L, "Leche", "L", 5.0, true, 7, 14);
        LoteResponse lote = new LoteResponse(1L, "L-LECHE-001", 30.0, 22.0,
                hoy.plusDays(10), "Lácteos Norte", BigDecimal.valueOf(1.20));

        when(stockService.getStockTodosInsumos()).thenReturn(List.of(
                new StockInsumoResponse(leche, 22.0, false, List.of(lote))));
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String merma = section(service.generarSnapshot(), "## Riesgo de merma por caducidad");
        assertThat(merma).contains("L-LECHE-001");
        // Sin consumo conocido, la merma probable iguala la cantidad del lote: 22.00 L.
        assertThat(merma).contains("22.00 L |\n");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Reposición urgente
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reposición urgente: solo URGENTE y ATENCION, descarta OK")
    void reposicionUrgente_filtraSoloUrgenteYAtencion() {
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of(
                new SugerenciaReposicionResponse(1L, "Café", "kg", 2.0, 1.5, 3, 10, 18.5, 1.3, "URGENTE"),
                new SugerenciaReposicionResponse(2L, "Leche", "L", 8.0, 2.0, 2, 7, 10.0, 4.0, "ATENCION"),
                new SugerenciaReposicionResponse(3L, "Azúcar", "kg", 20.0, 0.5, 5, 14, 0.0, 40.0, "OK")));
        when(stockService.getStockTodosInsumos()).thenReturn(List.of());
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String rep = section(service.generarSnapshot(), "## Reposición urgente");

        assertThat(rep).contains("Café").contains("Leche");
        assertThat(rep).doesNotContain("Azúcar");
    }

    @Test
    @DisplayName("reposición urgente: vacía → mensaje 'sin alertas'")
    void reposicionUrgente_vacia_muestraMensajeSano() {
        emptyDataset();
        String rep = section(service.generarSnapshot(), "## Reposición urgente");
        assertThat(rep).contains("Sin alertas");
    }

    @Test
    @DisplayName("reposición urgente: respeta el truncado MAX_FILAS_POR_SECCION")
    void reposicionUrgente_truncaAlLimite() {
        List<SugerenciaReposicionResponse> muchas = IntStream.range(0, 40)
                .mapToObj(i -> new SugerenciaReposicionResponse(
                        (long) i, "Crit" + i, "kg",
                        1.0, 0.5, 3, 10, 5.0, 2.0, "URGENTE"))
                .toList();
        when(reposicionService.calcularSugerencias(30)).thenReturn(muchas);
        when(stockService.getStockTodosInsumos()).thenReturn(List.of());
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String rep = section(service.generarSnapshot(), "## Reposición urgente");
        long filasCrit = rep.lines().filter(l -> l.startsWith("| Crit")).count();
        assertThat(filasCrit).isEqualTo(ContextoNegocioServiceImpl.MAX_FILAS_POR_SECCION);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Riesgo de faltantes
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("riesgo de faltantes: solo entradas con tieneRiesgoFaltante = true")
    void riesgoFaltantes_filtraConRiesgo() {
        InsumoResponse cafe = new InsumoResponse(1L, "Café", "kg", 5.0, true, 3, 10);
        InsumoResponse leche = new InsumoResponse(2L, "Leche", "L", 10.0, true, 2, 7);
        when(stockService.getStockTodosInsumos()).thenReturn(List.of(
                new StockInsumoResponse(cafe, 2.0, true, List.of()),
                new StockInsumoResponse(leche, 30.0, false, List.of())));
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String riesgo = section(service.generarSnapshot(), "## Riesgo de faltantes");
        assertThat(riesgo).contains("Café");
        assertThat(riesgo).doesNotContain("Leche");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Cobertura crítica
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cobertura crítica: solo días < 7 y finitos, orden ascendente")
    void coberturaCritica_filtraYOrdena() {
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of(
                new CoberturaInsumoResponse(1L, "Café", "kg", 5.0, 1.0, 5.0, "BAJO", 30),
                new CoberturaInsumoResponse(2L, "Leche", "L", 2.0, 1.0, 2.0, "CRITICO", 30),
                new CoberturaInsumoResponse(3L, "Vasos", "ud", 100.0, 5.0, 20.0, "OK", 30),
                new CoberturaInsumoResponse(4L, "Sirope", "L", 10.0, 0.0, Double.POSITIVE_INFINITY, "OK", 30)));
        when(stockService.getStockTodosInsumos()).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String crit = section(service.generarSnapshot(), "## Cobertura crítica");
        assertThat(crit).contains("Leche").contains("Café");
        assertThat(crit).doesNotContain("Vasos");
        assertThat(crit).doesNotContain("Sirope");
        assertThat(crit.indexOf("Leche")).isLessThan(crit.indexOf("Café"));
    }

    // ──────────────────────────────────────────────────────────────────────
    // Ventas
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ventas: agrega por producto dentro de la ventana y ordena descendente")
    void ventas_agregaYOrdena() {
        LocalDate hoy = LocalDate.now();
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of(
                new ReporteVentasDTO(hoy.minusDays(1), "Café con leche", 20L),
                new ReporteVentasDTO(hoy.minusDays(2), "Café con leche", 10L),
                new ReporteVentasDTO(hoy.minusDays(1), "Tostada", 5L),
                new ReporteVentasDTO(hoy.minusDays(120), "Café con leche", 999L)));
        when(stockService.getStockTodosInsumos()).thenReturn(List.of());
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());

        String top = section(service.generarSnapshot(), "## Top productos");
        assertThat(top).contains("| Café con leche | 30 |");
        int idxCafe = top.indexOf("Café con leche");
        int idxTostada = top.indexOf("Tostada");
        assertThat(idxCafe).isLessThan(idxTostada);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Tolerancia a fallos
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("fallo aislado de un servicio: la sección queda como 'Datos no disponibles' y el resto se genera")
    void fallo_unaSeccion_noTumbaElResto() {
        when(stockService.getStockTodosInsumos()).thenThrow(new RuntimeException("boom"));
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());

        String snapshot = service.generarSnapshot();

        // Las secciones que dependían del stock muestran "Datos no disponibles"
        assertThat(section(snapshot, "## Inventario actual")).contains("Datos no disponibles");
        // Pero la sección de ventas y reposición se generan normalmente
        assertThat(section(snapshot, "## Reposición urgente")).contains("Sin alertas");
        assertThat(section(snapshot, "## Top productos")).contains("No se han registrado ventas");
    }

    // ──────────────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────────────

    private void emptyDataset() {
        when(stockService.getStockTodosInsumos()).thenReturn(List.of());
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());
    }

    /** Extrae el bloque de texto comprendido entre {@code header} y el siguiente {@code "\n## "}. */
    private static String section(String snapshot, String header) {
        int start = snapshot.indexOf(header);
        if (start < 0) return "";
        int next = snapshot.indexOf("\n## ", start + header.length());
        return next < 0 ? snapshot.substring(start) : snapshot.substring(start, next);
    }
}
