package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.AjusteRequest;
import com.deusto.coffeestack.dto.MovimientoResponse;

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
}
