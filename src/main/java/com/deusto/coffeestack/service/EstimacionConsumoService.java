package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.EstimacionConsumoResponse;

/**
 * Servicio responsable de estimar el consumo diario futuro de un insumo
 * a partir de su histórico de movimientos que reducen stock.
 */
public interface EstimacionConsumoService {

    /**
     * Calcula la estimación de consumo para un insumo.
     *
     * @param insumoId      ID del insumo
     * @param ventanaDias   tamaño de la ventana de muestreo (debe ser &gt;= 1)
     * @param horizonteDias horizonte para la proyección (debe ser &gt;= 0)
     * @return DTO con consumo total, media diaria y proyección al horizonte
     * @throws com.deusto.coffeestack.exception.NotFoundException si el insumo no existe
     * @throws IllegalArgumentException si {@code ventanaDias} &lt; 1 o {@code horizonteDias} &lt; 0
     */
    EstimacionConsumoResponse calcular(Long insumoId, int ventanaDias, int horizonteDias);
}
