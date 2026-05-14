package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.ReporteComparativoResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReporteComparativoService {

    /**
     * Genera un reporte comparativo de consumo para una lista de insumos
     * en el rango [desde, hasta] con la granularidad indicada.
     *
     * <p>Si la lista de insumos está vacía o es nula se devuelven todos
     * los insumos activos del sistema.
     *
     * @param insumoIds    lista de IDs de insumos a comparar (puede ser vacía → todos)
     * @param desde        primer día incluido en el rango
     * @param hasta        último día incluido en el rango
     * @param granularidad agrupación temporal de cada serie devuelta
     * @return el reporte comparativo con una fila por insumo
     */
    ReporteComparativoResponse generar(List<Long> insumoIds, LocalDate desde,
                                       LocalDate hasta, Granularidad granularidad);
}
