package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.AjusteRequest;
import com.deusto.coffeestack.dto.MovimientoResponse;
import com.deusto.coffeestack.dto.ReporteMotivoResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for registering inventory adjustments
 * (mermas, roturas, positive and negative adjustments).
 */
public interface AjusteService {

    /**
     * Registers a stock movement, updates the affected batch's current quantity,
     * and persists an audit record.
     *
     * @param request       the adjustment details
     * @param usuarioLogin  login of the authenticated user performing the action
     * @return the persisted movement as a response DTO
     * @throws com.deusto.coffeestack.exception.NotFoundException if the lote does not exist
     * @throws IllegalArgumentException if cantidad exceeds available stock for negative movements
     */
    MovimientoResponse registrarAjuste(AjusteRequest request, String usuarioLogin);

    /** Returns all registered movements, most recent first. */
    List<MovimientoResponse> listarMovimientos();

    /**
     * Returns all movements for batches of a given insumo, most recent first.
     *
     * @throws com.deusto.coffeestack.exception.NotFoundException if the insumo does not exist
     */
    List<MovimientoResponse> listarMovimientosPorInsumo(Long insumoId);

    /**
     * Returns movements filtered by optional criteria, most recent first.
     *
     * @param insumoId restrict to a specific insumo (nullable)
     * @param tipo     restrict to a specific movement type (nullable)
     * @param desde    lower bound on fechaHora, inclusive (nullable)
     * @param hasta    upper bound on fechaHora, inclusive (nullable)
     */
    List<MovimientoResponse> listarMovimientosFiltrados(
            Long insumoId,
            TipoMovimiento tipo,
            LocalDateTime desde,
            LocalDateTime hasta);

    /**
     * Genera el reporte de movimientos agrupado por motivo y tipo, con el
     * número de incidencias y la cantidad total acumulada por cada combinación.
     * Pensado para que el propietario detecte patrones de desperdicio y enfoque
     * mejoras de proceso (issue #24).
     *
     * @param tipo  restringe a un tipo concreto (nullable: incluye todos)
     * @param desde inicio del rango, inclusivo (nullable)
     * @param hasta fin del rango, inclusivo (nullable)
     * @return filas ordenadas por cantidad total descendente
     */
    List<ReporteMotivoResponse> reportePorMotivo(
            TipoMovimiento tipo,
            LocalDateTime desde,
            LocalDateTime hasta);
}
