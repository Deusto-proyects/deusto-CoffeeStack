package com.deusto.coffeestack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Línea individual dentro de una {@link VentaRequest}:
 * qué producto y cuántas unidades se venden.
 */
public class VentaLineaRequest {

    @NotNull(message = "El itemId es obligatorio")
    private Long itemId;

    @Min(value = 1, message = "La cantidad de unidades debe ser al menos 1")
    private int cantidadUnidades;

    // ---- getters & setters ----

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public int getCantidadUnidades() { return cantidadUnidades; }
    public void setCantidadUnidades(int cantidadUnidades) { this.cantidadUnidades = cantidadUnidades; }
}
