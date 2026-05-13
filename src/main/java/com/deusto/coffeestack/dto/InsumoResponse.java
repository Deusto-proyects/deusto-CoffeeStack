package com.deusto.coffeestack.dto;

public class InsumoResponse {

    private Long id;
    private String nombre;
    private String unidadMedida;
    private double stockMinimoAlerta;
    private boolean activo;
    private int leadTimeDias;
    private int diasCobertura;

    public InsumoResponse(Long id,
                          String nombre,
                          String unidadMedida,
                          double stockMinimoAlerta,
                          boolean activo,
                          int leadTimeDias,
                          int diasCobertura) {
        this.id = id;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.stockMinimoAlerta = stockMinimoAlerta;
        this.activo = activo;
        this.leadTimeDias = leadTimeDias;
        this.diasCobertura = diasCobertura;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public double getStockMinimoAlerta() { return stockMinimoAlerta; }
    public boolean isActivo() { return activo; }
    public int getLeadTimeDias() { return leadTimeDias; }
    public int getDiasCobertura() { return diasCobertura; }
}
