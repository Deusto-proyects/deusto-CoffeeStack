package com.deusto.coffeestack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RecetaItemRequest {

    @NotNull(message = "El id del insumo es obligatorio")
    private Long insumoId;

    @Positive(message = "La cantidad debe ser mayor que cero")
    private double cantidad;

    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
}
