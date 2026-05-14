package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ConsumoPorTipoDTO;
import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.PuntoSerieDTO;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReporteConsumoServiceImpl implements ReporteConsumoService {

    private final InsumoRepository insumoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ReporteConsumoServiceImpl(InsumoRepository insumoRepository,
                                     MovimientoInventarioRepository movimientoRepository) {
        this.insumoRepository = insumoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteConsumoResponse generar(Long insumoId, LocalDate desde, LocalDate hasta,
                                          Granularidad granularidad) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' son obligatorias");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("'desde' debe ser anterior o igual a 'hasta'");
        }

        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + insumoId));

        LocalDateTime desdeDt = desde.atStartOfDay();
        LocalDateTime hastaDt = hasta.atTime(LocalTime.MAX);

        List<MovimientoInventario> movimientos =
                movimientoRepository.findMovimientosSalidaByInsumoAndRango(insumoId, desdeDt, hastaDt);

        // Desglose por tipo
        Map<TipoMovimiento, double[]> cantidadPorTipo = new EnumMap<>(TipoMovimiento.class);
        Map<TipoMovimiento, BigDecimal> costePorTipo = new EnumMap<>(TipoMovimiento.class);

        for (MovimientoInventario m : movimientos) {
            cantidadPorTipo.computeIfAbsent(m.getTipoMovimiento(), k -> new double[1])[0] += m.getCantidad();
            BigDecimal coste = costeDeMovimiento(m);
            costePorTipo.merge(m.getTipoMovimiento(), coste, BigDecimal::add);
        }

        List<ConsumoPorTipoDTO> desglose = new ArrayList<>();
        double totalCantidad = 0;
        BigDecimal costeTotal = BigDecimal.ZERO;
        for (Map.Entry<TipoMovimiento, double[]> e : cantidadPorTipo.entrySet()) {
            double cantidad = e.getValue()[0];
            BigDecimal coste = costePorTipo.getOrDefault(e.getKey(), BigDecimal.ZERO);
            desglose.add(new ConsumoPorTipoDTO(e.getKey(), cantidad, coste));
            totalCantidad += cantidad;
            costeTotal = costeTotal.add(coste);
        }

        // Serie temporal
        List<PuntoSerieDTO> serie = construirSerie(movimientos, granularidad);

        return new ReporteConsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                desde,
                hasta,
                granularidad,
                totalCantidad,
                costeTotal,
                desglose,
                serie
        );
    }

    private BigDecimal costeDeMovimiento(MovimientoInventario m) {
        BigDecimal precio = m.getLote() != null ? m.getLote().getPrecioCompra() : null;
        if (precio == null) return BigDecimal.ZERO;
        return precio.multiply(BigDecimal.valueOf(m.getCantidad()));
    }

    private List<PuntoSerieDTO> construirSerie(List<MovimientoInventario> movimientos,
                                               Granularidad granularidad) {
        // TreeMap garantiza orden cronológico
        Map<LocalDate, double[]> cantidad = new TreeMap<>();
        Map<LocalDate, BigDecimal> coste = new TreeMap<>();

        for (MovimientoInventario m : movimientos) {
            LocalDate clave = clavePorGranularidad(m.getFechaHora().toLocalDate(), granularidad);
            cantidad.computeIfAbsent(clave, k -> new double[1])[0] += m.getCantidad();
            coste.merge(clave, costeDeMovimiento(m), BigDecimal::add);
        }

        List<PuntoSerieDTO> serie = new ArrayList<>();
        for (Map.Entry<LocalDate, double[]> e : cantidad.entrySet()) {
            serie.add(new PuntoSerieDTO(
                    e.getKey(),
                    e.getValue()[0],
                    coste.getOrDefault(e.getKey(), BigDecimal.ZERO)
            ));
        }
        return serie;
    }

    private LocalDate clavePorGranularidad(LocalDate fecha, Granularidad granularidad) {
        if (granularidad == Granularidad.SEMANA) {
            // Lunes de la semana ISO que contiene la fecha
            return fecha.with(WeekFields.of(Locale.UK).dayOfWeek(), 1);
        }
        if (granularidad == Granularidad.MES) {
            // Primer día del mes
            return fecha.withDayOfMonth(1);
        }
        return fecha;
    }
}
