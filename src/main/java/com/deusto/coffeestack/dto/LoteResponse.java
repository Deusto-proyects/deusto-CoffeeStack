package com.deusto.coffeestack.dto;

import java.time.LocalDate;

public class LoteResponse {

    private Long id;
    private String numeroLote;
    private double cantidadInicial;
    private double cantidadActual;
    private LocalDate fechaVencimiento;
    private String proveedorNombre;

    public LoteResponse(Long id, String numeroLote, double cantidadInicial,
                        double cantidadActual, LocalDate fechaVencimiento, String proveedorNombre) {
        this.id = id;
        this.numeroLote = numeroLote;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.fechaVencimiento = fechaVencimiento;
        this.proveedorNombre = proveedorNombre;
    }

    public Long getId() { return id; }
    public String getNumeroLote() { return numeroLote; }
    public double getCantidadInicial() { return cantidadInicial; }
    public double getCantidadActual() { return cantidadActual; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public String getProveedorNombre() { return proveedorNombre; }
}
