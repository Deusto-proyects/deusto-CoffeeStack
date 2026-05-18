package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.CoberturaInsumoResponse;
import com.deusto.coffeestack.dto.LoteResponse;
import com.deusto.coffeestack.dto.ReporteVentasDTO;
import com.deusto.coffeestack.dto.StockInsumoResponse;
import com.deusto.coffeestack.dto.SugerenciaReposicionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Genera un snapshot Markdown con el estado operativo de la cafetería.
 *
 * <p>El snapshot está pensado para alimentar el contexto de un LLM, por lo
 * que combina dos tipos de secciones:
 * <ul>
 *   <li><b>Generales</b> (inventario actual, cobertura, caducidades próximas):
 *       casi siempre tienen datos y le dan al modelo algo concreto sobre lo
 *       que razonar, incluso cuando todo está sano.</li>
 *   <li><b>Alertas</b> (reposición urgente, riesgo de faltantes, cobertura
 *       crítica): muestran solo lo accionable.</li>
 * </ul>
 */
@Service
public class ContextoNegocioServiceImpl implements ContextoNegocioService {

    /** Tope de filas por sección para mantener el contexto compacto. */
    static final int MAX_FILAS_POR_SECCION = 25;

    /** Ventana en días usada para sugerencias y cobertura. */
    static final int VENTANA_DIAS = 30;

    /** Umbral en días para considerar cobertura "crítica". */
    static final int DIAS_COBERTURA_CRITICA = 7;

    /** Umbral en días para considerar caducidad "próxima". */
    static final int DIAS_CADUCIDAD_PROXIMA = 30;

    /** Top productos vendidos a destacar. */
    static final int TOP_PRODUCTOS = 10;

    /** Productos de menor rotación a destacar. */
    static final int BOTTOM_PRODUCTOS = 5;

    private final ReposicionService reposicionService;
    private final StockService stockService;
    private final VentaService ventaService;
    private final long ttlMillis;
    private final Clock clock;
    private final AtomicReference<CachedSnapshot> cache = new AtomicReference<>();

    @Autowired
    public ContextoNegocioServiceImpl(ReposicionService reposicionService,
                                      StockService stockService,
                                      VentaService ventaService,
                                      @Value("${ai.chatbot.snapshot-ttl-seconds:300}") long ttlSeconds) {
        this(reposicionService, stockService, ventaService, ttlSeconds, Clock.systemUTC());
    }

    ContextoNegocioServiceImpl(ReposicionService reposicionService,
                               StockService stockService,
                               VentaService ventaService,
                               long ttlSeconds,
                               Clock clock) {
        this.reposicionService = reposicionService;
        this.stockService = stockService;
        this.ventaService = ventaService;
        this.ttlMillis = Duration.ofSeconds(ttlSeconds).toMillis();
        this.clock = clock;
    }

    private record CachedSnapshot(String contenido, long timestamp) {}

    @Override
    @Transactional(readOnly = true)
    public String generarSnapshot() {
        long now = clock.millis();
        CachedSnapshot cached = cache.get();
        if (cached != null && now - cached.timestamp() < ttlMillis) {
            return cached.contenido();
        }
        String fresco = construirSnapshot();
        cache.set(new CachedSnapshot(fresco, now));
        return fresco;
    }

    private String construirSnapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append("## Fecha del snapshot: ").append(LocalDate.now()).append("\n\n");

        // Recolectamos una sola vez los datos compartidos por varias secciones,
        // así evitamos repetir consultas. Cualquier fallo aislado se traduce
        // en una sección "Datos no disponibles" sin tumbar el resto.
        List<StockInsumoResponse> stock = safeList(stockService::getStockTodosInsumos);
        List<CoberturaInsumoResponse> cobertura = safeList(() -> stockService.getCoberturaTodosInsumos(VENTANA_DIAS));

        appendInventarioGeneral(sb, stock);
        appendCoberturaGeneral(sb, cobertura);
        appendCaducidadesProximas(sb, stock, cobertura);
        appendReposicionUrgente(sb);
        appendRiesgoFaltantes(sb, stock);
        appendCoberturaCritica(sb, cobertura);
        appendVentas(sb);

        return sb.toString();
    }

    // ---- secciones siempre presentes ----

    private void appendInventarioGeneral(StringBuilder sb, List<StockInsumoResponse> stock) {
        sb.append("## Inventario actual\n");
        if (stock == null) { sb.append("> Datos no disponibles\n\n"); return; }
        if (stock.isEmpty()) { sb.append("> Sin insumos registrados\n\n"); return; }

        sb.append("| Insumo | Stock | Umbral alerta | Lead time | Cobertura objetivo |\n");
        sb.append("|---|---:|---:|---:|---:|\n");
        stock.stream().limit(MAX_FILAS_POR_SECCION).forEach(r -> {
            String nombre = r.getInsumo() != null ? r.getInsumo().getNombre() : "(sin nombre)";
            String unidad = r.getInsumo() != null ? r.getInsumo().getUnidadMedida() : "";
            double umbral = r.getInsumo() != null ? r.getInsumo().getStockMinimoAlerta() : 0.0;
            int lead = r.getInsumo() != null ? r.getInsumo().getLeadTimeDias() : 0;
            int objetivo = r.getInsumo() != null ? r.getInsumo().getDiasCobertura() : 0;
            sb.append("| ").append(nombre)
              .append(" | ").append(fmt(r.getCantidadTotal())).append(' ').append(unidad)
              .append(" | ").append(fmt(umbral)).append(' ').append(unidad)
              .append(" | ").append(lead).append(" d")
              .append(" | ").append(objetivo).append(" d")
              .append(" |\n");
        });
        sb.append('\n');
    }

    private void appendCoberturaGeneral(StringBuilder sb, List<CoberturaInsumoResponse> cobertura) {
        sb.append("## Cobertura por insumo (").append(VENTANA_DIAS).append(" días)\n");
        if (cobertura == null) { sb.append("> Datos no disponibles\n\n"); return; }
        if (cobertura.isEmpty()) { sb.append("> Sin datos\n\n"); return; }

        sb.append("| Insumo | Stock | Consumo/día | Cobertura | Riesgo |\n");
        sb.append("|---|---:|---:|---:|---|\n");
        cobertura.stream()
                .sorted(Comparator.comparingDouble(c -> Double.isInfinite(c.getDiasCobertura()) ? Double.MAX_VALUE : c.getDiasCobertura()))
                .limit(MAX_FILAS_POR_SECCION)
                .forEach(c -> sb.append("| ").append(c.getInsumoNombre())
                        .append(" | ").append(fmt(c.getStockActual())).append(' ').append(c.getUnidadMedida())
                        .append(" | ").append(fmt(c.getConsumoMedioDiario())).append(' ').append(c.getUnidadMedida())
                        .append(" | ").append(fmtDias(c.getDiasCobertura()))
                        .append(" | ").append(c.getNivelRiesgo())
                        .append(" |\n"));
        sb.append('\n');
    }

    private void appendCaducidadesProximas(StringBuilder sb,
                                           List<StockInsumoResponse> stock,
                                           List<CoberturaInsumoResponse> cobertura) {
        sb.append("## Riesgo de merma por caducidad (lotes que vencen en ≤ ")
          .append(DIAS_CADUCIDAD_PROXIMA).append(" días)\n");
        if (stock == null) { sb.append("> Datos no disponibles\n\n"); return; }

        // Mapa insumoId → consumo medio diario, para estimar la merma probable.
        Map<Long, Double> consumoPorInsumo = cobertura == null ? Map.of()
                : cobertura.stream().collect(Collectors.toMap(
                        CoberturaInsumoResponse::getInsumoId,
                        CoberturaInsumoResponse::getConsumoMedioDiario,
                        (a, b) -> a));

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_CADUCIDAD_PROXIMA);

        record Fila(String insumo, String lote, double cantidad, String unidad,
                    LocalDate vence, long diasRestantes, double consumoPrevisto,
                    double mermaProbable) {}
        List<Fila> filas = new ArrayList<>();

        for (StockInsumoResponse s : stock) {
            if (s.getLotes() == null || s.getInsumo() == null) continue;
            for (LoteResponse l : s.getLotes()) {
                if (l.getFechaVencimiento() == null) continue;
                if (l.getFechaVencimiento().isAfter(limite)) continue;
                if (l.getCantidadActual() <= 0) continue;
                long dias = ChronoUnit.DAYS.between(hoy, l.getFechaVencimiento());
                double consumoDiario = consumoPorInsumo.getOrDefault(s.getInsumo().getId(), 0.0);
                double consumoPrevisto = Math.max(0.0, consumoDiario) * Math.max(0L, dias);
                double mermaProbable = Math.max(0.0, l.getCantidadActual() - consumoPrevisto);
                filas.add(new Fila(
                        s.getInsumo().getNombre(),
                        l.getNumeroLote(),
                        l.getCantidadActual(),
                        s.getInsumo().getUnidadMedida(),
                        l.getFechaVencimiento(),
                        dias,
                        consumoPrevisto,
                        mermaProbable));
            }
        }
        // Orden: primero por merma probable descendente, luego por días restantes ascendente.
        filas.sort(Comparator
                .comparingDouble((Fila f) -> -f.mermaProbable())
                .thenComparingLong(Fila::diasRestantes));

        if (filas.isEmpty()) {
            sb.append("> Sin caducidades próximas — no se prevén mermas a corto plazo\n\n");
            return;
        }
        sb.append("| Insumo | Lote | Stock lote | Vence | Días | Consumo previsto | Merma probable |\n");
        sb.append("|---|---|---:|---|---:|---:|---:|\n");
        filas.stream().limit(MAX_FILAS_POR_SECCION).forEach(f ->
                sb.append("| ").append(f.insumo())
                  .append(" | ").append(f.lote())
                  .append(" | ").append(fmt(f.cantidad())).append(' ').append(f.unidad())
                  .append(" | ").append(f.vence())
                  .append(" | ").append(f.diasRestantes()).append(" d")
                  .append(" | ").append(fmt(f.consumoPrevisto())).append(' ').append(f.unidad())
                  .append(" | ").append(fmt(f.mermaProbable())).append(' ').append(f.unidad())
                  .append(" |\n"));
        sb.append('\n');
    }

    // ---- secciones de alertas (pueden quedar vacías cuando todo está sano) ----

    private void appendReposicionUrgente(StringBuilder sb) {
        sb.append("## Reposición urgente\n");
        List<SugerenciaReposicionResponse> sugerencias;
        try {
            sugerencias = reposicionService.calcularSugerencias(VENTANA_DIAS).stream()
                    .filter(s -> "URGENTE".equals(s.getNivelUrgencia())
                              || "ATENCION".equals(s.getNivelUrgencia()))
                    .sorted(Comparator.comparing(SugerenciaReposicionResponse::getNivelUrgencia))
                    .limit(MAX_FILAS_POR_SECCION)
                    .toList();
        } catch (RuntimeException ex) {
            sb.append("> Datos no disponibles\n\n");
            return;
        }

        if (sugerencias.isEmpty()) {
            sb.append("> Sin alertas — todos los insumos están con margen suficiente\n\n");
            return;
        }

        sb.append("| Insumo | Stock | Consumo/día | Lead time | Sugerido | Nivel |\n");
        sb.append("|---|---:|---:|---:|---:|---|\n");
        for (SugerenciaReposicionResponse s : sugerencias) {
            sb.append("| ").append(s.getInsumoNombre())
              .append(" | ").append(fmt(s.getStockActual())).append(' ').append(s.getUnidadMedida())
              .append(" | ").append(fmt(s.getConsumoMedioDiario())).append(' ').append(s.getUnidadMedida())
              .append(" | ").append(s.getLeadTimeDias()).append(" d")
              .append(" | ").append(fmt(s.getCantidadSugerida())).append(' ').append(s.getUnidadMedida())
              .append(" | ").append(s.getNivelUrgencia())
              .append(" |\n");
        }
        sb.append('\n');
    }

    private void appendRiesgoFaltantes(StringBuilder sb, List<StockInsumoResponse> stock) {
        sb.append("## Riesgo de faltantes\n");
        if (stock == null) { sb.append("> Datos no disponibles\n\n"); return; }

        List<StockInsumoResponse> riesgo = stock.stream()
                .filter(StockInsumoResponse::isTieneRiesgoFaltante)
                .limit(MAX_FILAS_POR_SECCION)
                .toList();

        if (riesgo.isEmpty()) {
            sb.append("> Ningún insumo está por debajo de su umbral de alerta\n\n");
            return;
        }

        sb.append("| Insumo | Stock total | Umbral alerta |\n");
        sb.append("|---|---:|---:|\n");
        for (StockInsumoResponse r : riesgo) {
            String nombre = r.getInsumo() != null ? r.getInsumo().getNombre() : "(sin nombre)";
            String unidad = r.getInsumo() != null ? r.getInsumo().getUnidadMedida() : "";
            double umbral = r.getInsumo() != null ? r.getInsumo().getStockMinimoAlerta() : 0.0;
            sb.append("| ").append(nombre)
              .append(" | ").append(fmt(r.getCantidadTotal())).append(' ').append(unidad)
              .append(" | ").append(fmt(umbral)).append(' ').append(unidad)
              .append(" |\n");
        }
        sb.append('\n');
    }

    private void appendCoberturaCritica(StringBuilder sb, List<CoberturaInsumoResponse> cobertura) {
        sb.append("## Cobertura crítica (< ").append(DIAS_COBERTURA_CRITICA).append(" días)\n");
        if (cobertura == null) { sb.append("> Datos no disponibles\n\n"); return; }

        List<CoberturaInsumoResponse> criticos = cobertura.stream()
                .filter(c -> Double.isFinite(c.getDiasCobertura()))
                .filter(c -> c.getDiasCobertura() < DIAS_COBERTURA_CRITICA)
                .sorted(Comparator.comparingDouble(CoberturaInsumoResponse::getDiasCobertura))
                .limit(MAX_FILAS_POR_SECCION)
                .toList();

        if (criticos.isEmpty()) {
            sb.append("> Ningún insumo tiene cobertura crítica\n\n");
            return;
        }

        sb.append("| Insumo | Stock | Consumo/día | Cobertura | Riesgo |\n");
        sb.append("|---|---:|---:|---:|---|\n");
        for (CoberturaInsumoResponse c : criticos) {
            sb.append("| ").append(c.getInsumoNombre())
              .append(" | ").append(fmt(c.getStockActual())).append(' ').append(c.getUnidadMedida())
              .append(" | ").append(fmt(c.getConsumoMedioDiario())).append(' ').append(c.getUnidadMedida())
              .append(" | ").append(fmt(c.getDiasCobertura())).append(" d")
              .append(" | ").append(c.getNivelRiesgo())
              .append(" |\n");
        }
        sb.append('\n');
    }

    private void appendVentas(StringBuilder sb) {
        List<ReporteVentasDTO> reporte;
        try {
            reporte = ventaService.obtenerReporteVentas();
        } catch (RuntimeException ex) {
            sb.append("## Top productos (últimos ").append(VENTANA_DIAS).append(" días)\n");
            sb.append("> Datos no disponibles\n\n");
            return;
        }

        LocalDate desde = LocalDate.now().minusDays(VENTANA_DIAS);
        Map<String, Long> ventasPorProducto = reporte.stream()
                .filter(r -> r.getFecha() != null && !r.getFecha().isBefore(desde))
                .filter(r -> r.getNombreProducto() != null && r.getCantidadTotal() != null)
                .collect(Collectors.groupingBy(
                        ReporteVentasDTO::getNombreProducto,
                        Collectors.summingLong(ReporteVentasDTO::getCantidadTotal)));

        List<Map.Entry<String, Long>> ordenadoDesc = ventasPorProducto.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        sb.append("## Top productos (últimos ").append(VENTANA_DIAS).append(" días)\n");
        if (ordenadoDesc.isEmpty()) {
            sb.append("> No se han registrado ventas formales en este periodo\n\n");
        } else {
            sb.append("| Producto | Unidades vendidas |\n");
            sb.append("|---|---:|\n");
            ordenadoDesc.stream().limit(TOP_PRODUCTOS).forEach(e ->
                    sb.append("| ").append(e.getKey()).append(" | ").append(e.getValue()).append(" |\n"));
            sb.append('\n');
        }

        if (ordenadoDesc.size() > TOP_PRODUCTOS) {
            sb.append("## Productos con baja rotación\n");
            sb.append("| Producto | Unidades vendidas |\n");
            sb.append("|---|---:|\n");
            ordenadoDesc.stream()
                    .skip(Math.max(0, ordenadoDesc.size() - BOTTOM_PRODUCTOS))
                    .forEach(e -> sb.append("| ").append(e.getKey()).append(" | ").append(e.getValue()).append(" |\n"));
            sb.append('\n');
        }
    }

    // ---- helpers ----

    private interface ListSupplier<T> { List<T> get(); }

    private static <T> List<T> safeList(ListSupplier<T> supplier) {
        try { return supplier.get(); }
        catch (RuntimeException ex) { return null; }
    }

    private static String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "—";
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }

    private static String fmtDias(double dias) {
        if (Double.isNaN(dias) || Double.isInfinite(dias)) return "∞";
        return String.format(java.util.Locale.ROOT, "%.1f d", dias);
    }
}
