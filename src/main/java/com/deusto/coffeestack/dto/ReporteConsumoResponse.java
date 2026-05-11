package com.deusto.coffeestack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReporteConsumoResponse {

    private Long insumoId;
    private String insumoNombre;
    private String unidadMedida;
    private LocalDate desde;
    private LocalDate hasta;
    private Granularidad granularidad;
    private double totalCantidad;
    private BigDecimal costeTotal;
    private List<ConsumoPorTipoDTO> desglosePorTipo;
    private List<PuntoSerieDTO> serie;

    public ReporteConsumoResponse(Long insumoId, String insumoNombre, String unidadMedida,
                                  LocalDate desde, LocalDate hasta, Granularidad granularidad,
                                  double totalCantidad, BigDecimal costeTotal,
                                  List<ConsumoPorTipoDTO> desglosePorTipo,
                                  List<PuntoSerieDTO> serie) {
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.unidadMedida = unidadMedida;
        this.desde = desde;
        this.hasta = hasta;
        this.granularidad = granularidad;
        this.totalCantidad = totalCantidad;
        this.costeTotal = costeTotal;
        this.desglosePorTipo = desglosePorTipo;
        this.serie = serie;
    }

    public Long getInsumoId() { return insumoId; }
    public String getInsumoNombre() { return insumoNombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public Granularidad getGranularidad() { return granularidad; }
    public double getTotalCantidad() { return totalCantidad; }
    public BigDecimal getCosteTotal() { return costeTotal; }
    public List<ConsumoPorTipoDTO> getDesglosePorTipo() { return desglosePorTipo; }
    public List<PuntoSerieDTO> getSerie() { return serie; }
}
