package com.deusto.coffeestack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de consumo comparativo para múltiples insumos en un rango de fechas.
 *
 * <p>Permite al propietario visualizar y comparar el consumo y coste de varios
 * insumos de forma simultánea para identificar patrones operativos.
 */
public class ReporteComparativoResponse {

    /** Insumo analizado dentro del comparativo. */
    public static class FilaInsumo {
        private Long insumoId;
        private String insumoNombre;
        private String unidadMedida;
        private double totalCantidad;
        private BigDecimal costeTotal;
        /** Serie temporal del insumo con la granularidad solicitada. */
        private List<PuntoSerieDTO> serie;

        public FilaInsumo(Long insumoId, String insumoNombre, String unidadMedida,
                          double totalCantidad, BigDecimal costeTotal, List<PuntoSerieDTO> serie) {
            this.insumoId = insumoId;
            this.insumoNombre = insumoNombre;
            this.unidadMedida = unidadMedida;
            this.totalCantidad = totalCantidad;
            this.costeTotal = costeTotal;
            this.serie = serie;
        }

        public Long getInsumoId() { return insumoId; }
        public String getInsumoNombre() { return insumoNombre; }
        public String getUnidadMedida() { return unidadMedida; }
        public double getTotalCantidad() { return totalCantidad; }
        public BigDecimal getCosteTotal() { return costeTotal; }
        public List<PuntoSerieDTO> getSerie() { return serie; }
    }

    private LocalDate desde;
    private LocalDate hasta;
    private Granularidad granularidad;
    /** Coste total sumado de todos los insumos del comparativo. */
    private BigDecimal costeTotalGlobal;
    private List<FilaInsumo> insumos;

    public ReporteComparativoResponse(LocalDate desde, LocalDate hasta,
                                      Granularidad granularidad,
                                      BigDecimal costeTotalGlobal,
                                      List<FilaInsumo> insumos) {
        this.desde = desde;
        this.hasta = hasta;
        this.granularidad = granularidad;
        this.costeTotalGlobal = costeTotalGlobal;
        this.insumos = insumos;
    }

    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public Granularidad getGranularidad() { return granularidad; }
    public BigDecimal getCosteTotalGlobal() { return costeTotalGlobal; }
    public List<FilaInsumo> getInsumos() { return insumos; }
}
