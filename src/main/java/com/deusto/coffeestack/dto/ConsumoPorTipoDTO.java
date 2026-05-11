package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.domain.TipoMovimiento;

import java.math.BigDecimal;

public class ConsumoPorTipoDTO {

    private TipoMovimiento tipo;
    private double cantidad;
    private BigDecimal coste;

    public ConsumoPorTipoDTO(TipoMovimiento tipo, double cantidad, BigDecimal coste) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.coste = coste;
    }

    public TipoMovimiento getTipo() { return tipo; }
    public double getCantidad() { return cantidad; }
    public BigDecimal getCoste() { return coste; }
}
