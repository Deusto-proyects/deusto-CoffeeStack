package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.TipoMovimiento;
import jakarta.validation.constraints.*;

/**
 * Request body to register a stock movement (merma, rotura or adjustment).
 */
public class AjusteRequest {

    @NotNull(message = "El loteId es obligatorio")
    private Long loteId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @Positive(message = "La cantidad debe ser mayor que cero")
    private double cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres")
    private String motivo;

    // ---- getters & setters ----

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
