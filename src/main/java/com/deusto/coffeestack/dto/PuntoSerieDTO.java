package com.deusto.coffeestack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PuntoSerieDTO {

    private LocalDate fecha;
    private double cantidad;
    private BigDecimal coste;

    public PuntoSerieDTO(LocalDate fecha, double cantidad, BigDecimal coste) {
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.coste = coste;
    }

    public LocalDate getFecha() { return fecha; }
    public double getCantidad() { return cantidad; }
    public BigDecimal getCoste() { return coste; }
}
