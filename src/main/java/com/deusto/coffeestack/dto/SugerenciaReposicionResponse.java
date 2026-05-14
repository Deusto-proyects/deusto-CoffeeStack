package com.deusto.coffeestack.dto;

/**
 * Sugerencia de reposición para un insumo.
 *
 * <p>Combina el stock actual, el consumo medio diario reciente y los
 * parámetros del insumo (lead time del proveedor y días de cobertura
 * objetivo) para proponer una cantidad a comprar y un nivel de urgencia.
 *
 * <p><b>Fórmula:</b>
 * {@code cantidadSugerida = max(0, consumoMedioDiario * (leadTimeDias + diasCobertura) - stockActual)}
 *
 * <p><b>Niveles de urgencia:</b>
 * <ul>
 *   <li>{@code URGENTE} – el stock actual no cubre ni el lead time del proveedor.</li>
 *   <li>{@code ATENCION} – el stock cubre el lead time pero menos de la mitad
 *       de la cobertura objetivo.</li>
 *   <li>{@code OK} – el stock cubre lead time + media cobertura, o no hay
 *       consumo registrado.</li>
 * </ul>
 *
 * <p>{@code diasCoberturaRestante} es {@code -1} cuando no hay consumo
 * registrado (división por cero), lo que el frontend puede representar
 * como "infinito" o "indeterminado".
 */
public class SugerenciaReposicionResponse {

    private Long insumoId;
    private String insumoNombre;
    private String unidadMedida;
    private double stockActual;
    private double consumoMedioDiario;
    private int leadTimeDias;
    private int diasCobertura;
    private double cantidadSugerida;
    private double diasCoberturaRestante;
    private String nivelUrgencia;

    public SugerenciaReposicionResponse(Long insumoId,
                                        String insumoNombre,
                                        String unidadMedida,
                                        double stockActual,
                                        double consumoMedioDiario,
                                        int leadTimeDias,
                                        int diasCobertura,
                                        double cantidadSugerida,
                                        double diasCoberturaRestante,
                                        String nivelUrgencia) {
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.consumoMedioDiario = consumoMedioDiario;
        this.leadTimeDias = leadTimeDias;
        this.diasCobertura = diasCobertura;
        this.cantidadSugerida = cantidadSugerida;
        this.diasCoberturaRestante = diasCoberturaRestante;
        this.nivelUrgencia = nivelUrgencia;
    }

    public Long getInsumoId()               { return insumoId; }
    public String getInsumoNombre()         { return insumoNombre; }
    public String getUnidadMedida()         { return unidadMedida; }
    public double getStockActual()          { return stockActual; }
    public double getConsumoMedioDiario()   { return consumoMedioDiario; }
    public int getLeadTimeDias()            { return leadTimeDias; }
    public int getDiasCobertura()           { return diasCobertura; }
    public double getCantidadSugerida()     { return cantidadSugerida; }
    public double getDiasCoberturaRestante(){ return diasCoberturaRestante; }
    public String getNivelUrgencia()        { return nivelUrgencia; }
}
