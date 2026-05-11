package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;

import java.time.LocalDate;

public interface ReporteConsumoService {

    /**
     * Genera un reporte de consumo (cantidad + coste estimado) de un insumo
     * en el rango [desde, hasta] (ambos inclusive, días enteros).
     *
     * @param insumoId      id del insumo
     * @param desde         primer día incluido en el rango
     * @param hasta         último día incluido en el rango
     * @param granularidad  agrupación temporal de la serie devuelta
     * @return el reporte con totales, desglose por tipo y serie temporal
     */
    ReporteConsumoResponse generar(Long insumoId, LocalDate desde, LocalDate hasta, Granularidad granularidad);
}
