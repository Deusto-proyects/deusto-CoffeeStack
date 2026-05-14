package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.PuntoSerieDTO;
import com.deusto.coffeestack.dto.ReporteComparativoResponse;
import com.deusto.coffeestack.dto.ReporteComparativoResponse.FilaInsumo;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReporteComparativoServiceImpl implements ReporteComparativoService {

    private final InsumoRepository insumoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ReporteComparativoServiceImpl(InsumoRepository insumoRepository,
                                         MovimientoInventarioRepository movimientoRepository) {
        this.insumoRepository = insumoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteComparativoResponse generar(List<Long> insumoIds, LocalDate desde,
                                              LocalDate hasta, Granularidad granularidad) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' son obligatorias");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("'desde' debe ser anterior o igual a 'hasta'");
        }

        // Si no se especifican insumos, cargamos todos los activos
        List<Insumo> insumos;
        if (insumoIds == null || insumoIds.isEmpty()) {
            insumos = insumoRepository.findAll().stream()
                    .filter(Insumo::isActivo)
                    .toList();
        } else {
            insumos = insumoRepository.findAllById(insumoIds);
        }

        LocalDateTime desdeDt = desde.atStartOfDay();
        LocalDateTime hastaDt = hasta.atTime(LocalTime.MAX);

        BigDecimal costeTotalGlobal = BigDecimal.ZERO;
        List<FilaInsumo> filas = new ArrayList<>();

        for (Insumo insumo : insumos) {
            List<MovimientoInventario> movimientos =
                    movimientoRepository.findMovimientosSalidaByInsumoAndRango(
                            insumo.getId(), desdeDt, hastaDt);

            double totalCantidad = 0;
            BigDecimal costeTotal = BigDecimal.ZERO;
            Map<LocalDate, double[]> cantidadPorClave = new TreeMap<>();
            Map<LocalDate, BigDecimal> costePorClave = new TreeMap<>();

            for (MovimientoInventario m : movimientos) {
                totalCantidad += m.getCantidad();
                BigDecimal coste = costeDeMovimiento(m);
                costeTotal = costeTotal.add(coste);

                LocalDate clave = clavePorGranularidad(m.getFechaHora().toLocalDate(), granularidad);
                cantidadPorClave.computeIfAbsent(clave, k -> new double[1])[0] += m.getCantidad();
                costePorClave.merge(clave, coste, BigDecimal::add);
            }

            List<PuntoSerieDTO> serie = new ArrayList<>();
            for (Map.Entry<LocalDate, double[]> e : cantidadPorClave.entrySet()) {
                serie.add(new PuntoSerieDTO(
                        e.getKey(),
                        e.getValue()[0],
                        costePorClave.getOrDefault(e.getKey(), BigDecimal.ZERO)
                ));
            }

            costeTotalGlobal = costeTotalGlobal.add(costeTotal);
            filas.add(new FilaInsumo(
                    insumo.getId(),
                    insumo.getNombre(),
                    insumo.getUnidadMedida(),
                    totalCantidad,
                    costeTotal,
                    serie
            ));
        }

        return new ReporteComparativoResponse(desde, hasta, granularidad, costeTotalGlobal, filas);
    }

    private BigDecimal costeDeMovimiento(MovimientoInventario m) {
        BigDecimal precio = m.getLote() != null ? m.getLote().getPrecioCompra() : null;
        if (precio == null) return BigDecimal.ZERO;
        return precio.multiply(BigDecimal.valueOf(m.getCantidad()));
    }

    private LocalDate clavePorGranularidad(LocalDate fecha, Granularidad granularidad) {
        if (granularidad == Granularidad.SEMANA) {
            return fecha.with(WeekFields.of(Locale.UK).dayOfWeek(), 1);
        }
        if (granularidad == Granularidad.MES) {
            return fecha.withDayOfMonth(1);
        }
        return fecha;
    }
}
