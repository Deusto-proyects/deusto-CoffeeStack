package com.deusto.coffeestack.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Petición para registrar una nueva venta.
 *
 * <p>Contiene al menos una línea de detalle con el producto y la cantidad vendida.
 */
public class VentaRequest {

    @NotEmpty(message = "La venta debe tener al menos una línea")
    @Valid
    private List<VentaLineaRequest> lineas;

    // ---- getters & setters ----

    public List<VentaLineaRequest> getLineas() { return lineas; }
    public void setLineas(List<VentaLineaRequest> lineas) { this.lineas = lineas; }
}
