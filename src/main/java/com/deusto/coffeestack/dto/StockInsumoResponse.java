package com.deusto.coffeestack.dto;

import java.util.List;

/**
 * Consolidated stock view for a single Insumo.
 *
 * <p><b>tieneRiesgoFaltante</b> is {@code true} when the total available
 * quantity ({@code cantidadTotal}) is below the insumo's
 * {@code stockMinimoAlerta} threshold.
 */
public class StockInsumoResponse {

    private InsumoResponse insumo;
    private double cantidadTotal;
    private boolean tieneRiesgoFaltante;
    private List<LoteResponse> lotes;

    public StockInsumoResponse(InsumoResponse insumo,
                               double cantidadTotal,
                               boolean tieneRiesgoFaltante,
                               List<LoteResponse> lotes) {
        this.insumo = insumo;
        this.cantidadTotal = cantidadTotal;
        this.tieneRiesgoFaltante = tieneRiesgoFaltante;
        this.lotes = lotes;
    }

    public InsumoResponse getInsumo() { return insumo; }
    public double getCantidadTotal() { return cantidadTotal; }
    public boolean isTieneRiesgoFaltante() { return tieneRiesgoFaltante; }
    public List<LoteResponse> getLotes() { return lotes; }
}
