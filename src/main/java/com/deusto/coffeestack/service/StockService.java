package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.CoberturaInsumoResponse;
import com.deusto.coffeestack.dto.StockInsumoResponse;

import java.util.List;

/**
 * Service responsible for stock consultation per insumo and per batch (lote).
 *
 * <p>Business rules implemented:
 * <ul>
 *   <li>Total stock = sum of {@code cantidadActual} across all lotes of an insumo.</li>
 *   <li>Shortage risk = total stock &lt; insumo's {@code stockMinimoAlerta}.</li>
 * </ul>
 */
public interface StockService {

    /**
     * Returns the detailed stock for a single insumo, including its batch breakdown.
     *
     * @param insumoId the ID of the insumo
     * @return stock detail with lote breakdown and shortage risk flag
     * @throws com.deusto.coffeestack.exception.NotFoundException if the insumo does not exist
     */
    StockInsumoResponse getStockDetalladoPorInsumo(Long insumoId);

    /**
     * Returns the stock summary for every registered insumo.
     */
    List<StockInsumoResponse> getStockTodosInsumos();

    /**
     * Returns the estimated coverage days for every registered insumo.
     *
     * <p>Coverage days = total stock / average daily consumption computed
     * over the given {@code ventanaDias} window.
     * If consumption is zero the result is {@link Double#POSITIVE_INFINITY}
     * and the risk level is set to {@code OK}.
     *
     * @param ventanaDias sampling window in days (must be &gt;= 1)
     */
    List<CoberturaInsumoResponse> getCoberturaTodosInsumos(int ventanaDias);
}
