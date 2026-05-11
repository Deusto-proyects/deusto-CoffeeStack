package com.deusto.coffeestack.dto;

/**
 * Días de cobertura estimados para un insumo.
 *
 * <p>Combina el stock actual del insumo con su consumo medio diario
 * (calculado sobre una ventana histórica) para estimar cuántos días
 * de operación están cubiertos con el inventario disponible.
 *
 * <p><b>Fórmula:</b> {@code diasCobertura = stockActual / consumoMedioDiario}
 *
 * <p>Si {@code consumoMedioDiario} es 0 (sin historial de salidas), se devuelve
 * {@link Double#POSITIVE_INFINITY} para indicar que no hay consumo registrado
 * y el riesgo es indeterminado (nivel {@code OK} por defecto).
 *
 * <p><b>Niveles de riesgo:</b>
 * <ul>
 *   <li>{@code CRITICO} – menos de 3 días de cobertura.</li>
 *   <li>{@code BAJO}    – entre 3 y 6 días (inclusive).</li>
 *   <li>{@code OK}      – 7 o más días, o consumo cero.</li>
 * </ul>
 */
public class CoberturaInsumoResponse {

    private Long insumoId;
    private String insumoNombre;
    private String unidadMedida;

    /** Cantidad total disponible en todos los lotes activos del insumo. */
    private double stockActual;

    /** Consumo medio diario calculado sobre la ventana solicitada. */
    private double consumoMedioDiario;

    /**
     * Días estimados de cobertura.
     * Puede ser {@link Double#POSITIVE_INFINITY} si no hay consumo registrado.
     */
    private double diasCobertura;

    /** {@code CRITICO}, {@code BAJO} u {@code OK}. */
    private String nivelRiesgo;

    /** Ventana en días usada para calcular el consumo medio. */
    private int ventanaDias;

    public CoberturaInsumoResponse(Long insumoId,
                                   String insumoNombre,
                                   String unidadMedida,
                                   double stockActual,
                                   double consumoMedioDiario,
                                   double diasCobertura,
                                   String nivelRiesgo,
                                   int ventanaDias) {
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.consumoMedioDiario = consumoMedioDiario;
        this.diasCobertura = diasCobertura;
        this.nivelRiesgo = nivelRiesgo;
        this.ventanaDias = ventanaDias;
    }

    // ---- getters ----

    public Long getInsumoId()             { return insumoId; }
    public String getInsumoNombre()       { return insumoNombre; }
    public String getUnidadMedida()       { return unidadMedida; }
    public double getStockActual()        { return stockActual; }
    public double getConsumoMedioDiario() { return consumoMedioDiario; }
    public double getDiasCobertura()      { return diasCobertura; }
    public String getNivelRiesgo()        { return nivelRiesgo; }
    public int getVentanaDias()           { return ventanaDias; }
}
