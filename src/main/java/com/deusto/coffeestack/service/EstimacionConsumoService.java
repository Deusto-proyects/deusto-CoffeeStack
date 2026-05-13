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

    /**
     * Devuelve solamente el consumo medio diario de un insumo sobre la ventana
     * indicada, sin construir el DTO completo.
     *
     * <p>Pensado para ser reutilizado por otros servicios (sugerencias de
     * reposición, cobertura, etc.) que únicamente necesitan el ritmo medio
     * y no la proyección.
     *
     * @param insumoId    ID del insumo
     * @param ventanaDias tamaño de la ventana de muestreo (debe ser &gt;= 1)
     * @return consumo medio diario (0 si no hay movimientos en la ventana)
     * @throws IllegalArgumentException si {@code ventanaDias} &lt; 1
     */
    double calcularConsumoMedioDiario(Long insumoId, int ventanaDias);
}
