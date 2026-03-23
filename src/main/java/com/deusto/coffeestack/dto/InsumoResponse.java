package com.deusto.coffeestack.dto;

public class InsumoResponse {

    private Long id;
    private String nombre;
    private String unidadMedida;
    private double stockMinimoAlerta;

    public InsumoResponse(Long id, String nombre, String unidadMedida, double stockMinimoAlerta) {
        this.id = id;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.stockMinimoAlerta = stockMinimoAlerta;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public double getStockMinimoAlerta() { return stockMinimoAlerta; }
}
