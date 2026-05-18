package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.TipoMovimiento;

import java.time.LocalDateTime;

/**
 * Fila de un reporte de movimientos agrupado por motivo y tipo.
 *
 * <p>Permite al propietario detectar patrones de desperdicio (p. ej. cuántos kg
 * se pierden recurrentemente por "Caducidad lote leche") y enfocar mejoras de
 * proceso sobre los motivos más costosos.
 *
 * <p>La consulta JPQL que la alimenta agrupa por {@code motivo} y
 * {@code tipoMovimiento}, suma {@code cantidad} y cuenta incidencias.
 */
public class ReporteMotivoResponse {

    private String motivo;
    private TipoMovimiento tipoMovimiento;
    private long numIncidencias;
    private double cantidadTotal;
    private LocalDateTime primeraFecha;
    private LocalDateTime ultimaFecha;

    public ReporteMotivoResponse(String motivo,
                                 TipoMovimiento tipoMovimiento,
                                 long numIncidencias,
                                 double cantidadTotal,
                                 LocalDateTime primeraFecha,
                                 LocalDateTime ultimaFecha) {
        this.motivo = motivo;
        this.tipoMovimiento = tipoMovimiento;
        this.numIncidencias = numIncidencias;
        this.cantidadTotal = cantidadTotal;
        this.primeraFecha = primeraFecha;
        this.ultimaFecha = ultimaFecha;
    }

    public String getMotivo() { return motivo; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public long getNumIncidencias() { return numIncidencias; }
    public double getCantidadTotal() { return cantidadTotal; }
    public LocalDateTime getPrimeraFecha() { return primeraFecha; }
    public LocalDateTime getUltimaFecha() { return ultimaFecha; }
}
