package com.deusto.coffeestack.dto;

/**
 * Estimación de consumo diario futuro para un insumo.
 *
 * <p>Los movimientos considerados son los que reducen stock:
 * {@code VENTA}, {@code MERMA}, {@code ROTURA} y {@code AJUSTE_NEGATIVO}.
 *
 * <p>{@code consumoMedioDiario} se obtiene como
 * {@code consumoTotalVentana / ventanaDias} y {@code consumoProyectado}
 * como {@code consumoMedioDiario * horizonteDias}.
 */
public class EstimacionConsumoResponse {

    private Long insumoId;
    private String insumoNombre;
    private String unidadMedida;
    private int ventanaDias;
    private double consumoTotalVentana;
    private double consumoMedioDiario;
    private int diasConActividad;
    private int horizonteDias;
    private double consumoProyectado;

    public EstimacionConsumoResponse(Long insumoId,
                                     String insumoNombre,
                                     String unidadMedida,
                                     int ventanaDias,
                                     double consumoTotalVentana,
                                     double consumoMedioDiario,
                                     int diasConActividad,
                                     int horizonteDias,
                                     double consumoProyectado) {
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.unidadMedida = unidadMedida;
        this.ventanaDias = ventanaDias;
        this.consumoTotalVentana = consumoTotalVentana;
        this.consumoMedioDiario = consumoMedioDiario;
        this.diasConActividad = diasConActividad;
        this.horizonteDias = horizonteDias;
        this.consumoProyectado = consumoProyectado;
    }

    public Long getInsumoId() { return insumoId; }
    public String getInsumoNombre() { return insumoNombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public int getVentanaDias() { return ventanaDias; }
    public double getConsumoTotalVentana() { return consumoTotalVentana; }
    public double getConsumoMedioDiario() { return consumoMedioDiario; }
    public int getDiasConActividad() { return diasConActividad; }
    public int getHorizonteDias() { return horizonteDias; }
    public double getConsumoProyectado() { return consumoProyectado; }
}
