package com.deusto.coffeestack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class InsumoUpdateRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;

    @NotBlank(message = "La unidad de medida no puede estar vacía")
    @Size(max = 30, message = "La unidad de medida no puede superar 30 caracteres")
    private String unidadMedida;

    @Positive(message = "El stock mínimo de alerta debe ser positivo")
    private double stockMinimoAlerta;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public double getStockMinimoAlerta() { return stockMinimoAlerta; }
    public void setStockMinimoAlerta(double stockMinimoAlerta) { this.stockMinimoAlerta = stockMinimoAlerta; }
}
