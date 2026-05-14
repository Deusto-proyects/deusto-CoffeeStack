package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.SugerenciaReposicionResponse;

import java.util.List;

/**
 * Servicio que genera sugerencias de reposición de insumos.
 *
 * <p>Apoyado en {@link EstimacionConsumoService} para el consumo medio diario
 * y en los parámetros {@code leadTimeDias} y {@code diasCobertura} configurados
 * en cada insumo, propone una cantidad a comprar y clasifica la urgencia.
 */
public interface ReposicionService {

    /**
     * Calcula las sugerencias de reposición para todos los insumos activos.
     *
     * @param ventanaConsumoDias días hacia atrás que se usan para estimar la
     *                           media diaria de consumo (debe ser &gt; 0)
     * @return lista de sugerencias, una por insumo activo
     * @throws IllegalArgumentException si {@code ventanaConsumoDias} &lt;= 0
     */
    List<SugerenciaReposicionResponse> calcularSugerencias(int ventanaConsumoDias);

    /**
     * Calcula la sugerencia de reposición para un único insumo.
     *
     * @param insumoId           ID del insumo
     * @param ventanaConsumoDias días hacia atrás que se usan para estimar la
     *                           media diaria de consumo (debe ser &gt; 0)
     * @return la sugerencia para ese insumo
     * @throws com.deusto.coffeestack.exception.NotFoundException si el insumo no existe
     * @throws IllegalArgumentException si {@code ventanaConsumoDias} &lt;= 0
     */
    SugerenciaReposicionResponse calcularSugerenciaPorInsumo(Long insumoId, int ventanaConsumoDias);
}
