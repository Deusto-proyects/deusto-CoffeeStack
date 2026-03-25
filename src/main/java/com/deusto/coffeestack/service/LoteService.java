package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.LoteCreateRequest;
import com.deusto.coffeestack.dto.LoteResponse;

import java.util.List;

/**
 * Service operations related to supply batch (lote) reception and traceability.
 */
public interface LoteService {

    /** Registers the reception of a new batch. Returns the persisted batch response. */
    LoteResponse recibirLote(LoteCreateRequest request);

    /** Lists all batches for a given insumo, ordered by expiry date ascending (nulls last). */
    List<LoteResponse> listarPorInsumo(Long insumoId);

    /** Retrieves a single batch by its ID. Throws NotFoundException if not found. */
    LoteResponse obtenerPorId(Long id);
}
