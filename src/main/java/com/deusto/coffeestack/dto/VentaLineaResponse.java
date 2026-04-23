package com.deusto.coffeestack.dto;

/**
 * Detalle de una línea de venta devuelto en la respuesta.
 */
public class VentaLineaResponse {

    private Long itemId;
    private String itemNombre;
    private int cantidadUnidades;

    public VentaLineaResponse(Long itemId, String itemNombre, int cantidadUnidades) {
        this.itemId = itemId;
        this.itemNombre = itemNombre;
        this.cantidadUnidades = cantidadUnidades;
    }

    // ---- getters ----

    public Long getItemId() { return itemId; }
    public String getItemNombre() { return itemNombre; }
    public int getCantidadUnidades() { return cantidadUnidades; }
}
