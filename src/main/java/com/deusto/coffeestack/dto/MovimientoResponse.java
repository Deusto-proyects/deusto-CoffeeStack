package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.TipoMovimiento;
import java.time.LocalDateTime;

/**
 * Response DTO for a single inventory movement (merma, rotura or adjustment).
 */
public class MovimientoResponse {

    private Long id;
    private Long loteId;
    private String numeroLote;
    private String insumoNombre;
    private TipoMovimiento tipoMovimiento;
    private double cantidad;
    private String motivo;
    private String usuario;
    private LocalDateTime fechaHora;

    public MovimientoResponse(Long id,
                              Long loteId,
                              String numeroLote,
                              String insumoNombre,
                              TipoMovimiento tipoMovimiento,
                              double cantidad,
                              String motivo,
                              String usuario,
                              LocalDateTime fechaHora) {
        this.id = id;
        this.loteId = loteId;
        this.numeroLote = numeroLote;
        this.insumoNombre = insumoNombre;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
    }

    // ---- getters ----

    public Long getId() { return id; }
    public Long getLoteId() { return loteId; }
    public String getNumeroLote() { return numeroLote; }
    public String getInsumoNombre() { return insumoNombre; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public double getCantidad() { return cantidad; }
    public String getMotivo() { return motivo; }
    public String getUsuario() { return usuario; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
