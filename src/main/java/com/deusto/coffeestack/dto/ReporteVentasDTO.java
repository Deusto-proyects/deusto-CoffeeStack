package com.deusto.coffeestack.dto;

import java.time.LocalDate;

public class ReporteVentasDTO {
    private LocalDate fecha;
    private String nombreProducto;
    private Long cantidadTotal;

    public ReporteVentasDTO() {
    }

    public ReporteVentasDTO(java.sql.Date fecha, String nombreProducto, Long cantidadTotal) {
        this.fecha = fecha != null ? fecha.toLocalDate() : null;
        this.nombreProducto = nombreProducto;
        this.cantidadTotal = cantidadTotal;
    }

    public ReporteVentasDTO(LocalDate fecha, String nombreProducto, Long cantidadTotal) {
        this.fecha = fecha;
        this.nombreProducto = nombreProducto;
        this.cantidadTotal = cantidadTotal;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Long getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(Long cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }
}
