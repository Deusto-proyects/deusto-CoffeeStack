package com.deusto.coffeestack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoteResponse {

    private Long id;
    private String numeroLote;
    private double cantidadInicial;
    private double cantidadActual;
    private LocalDate fechaVencimiento;
    private String proveedorNombre;
    private BigDecimal precioCompra;

    public LoteResponse(Long id, String numeroLote, double cantidadInicial,
                        double cantidadActual, LocalDate fechaVencimiento, String proveedorNombre,
                        BigDecimal precioCompra) {
        this.id = id;
        this.numeroLote = numeroLote;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.fechaVencimiento = fechaVencimiento;
        this.proveedorNombre = proveedorNombre;
        this.precioCompra = precioCompra;
    }

    public Long getId() { return id; }
    public String getNumeroLote() { return numeroLote; }
    public double getCantidadInicial() { return cantidadInicial; }
    public double getCantidadActual() { return cantidadActual; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public String getProveedorNombre() { return proveedorNombre; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
}
