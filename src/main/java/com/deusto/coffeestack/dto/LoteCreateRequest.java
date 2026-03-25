package com.deusto.coffeestack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request payload for registering the reception of a supply batch (lote).
 */
public class LoteCreateRequest {

    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;

    /** Optional: supplier that delivered this batch. */
    private Long proveedorId;

    @NotBlank(message = "El número de lote es obligatorio")
    @Size(max = 60, message = "El número de lote no puede superar los 60 caracteres")
    private String numeroLote;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Double cantidad;

    /** Optional: null means the supply does not expire. */
    private LocalDate fechaVencimiento;

    // ---- getters & setters ----

    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
