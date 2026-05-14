package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.dto.SugerenciaReposicionResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReposicionServiceImpl implements ReposicionService {

    private static final String NIVEL_URGENTE  = "URGENTE";
    private static final String NIVEL_ATENCION = "ATENCION";
    private static final String NIVEL_OK       = "OK";

    /** Sentinela usado cuando no hay consumo y la cobertura restante es infinita. */
    private static final double COBERTURA_INDETERMINADA = -1.0;

    private final InsumoRepository insumoRepository;
    private final LoteRepository loteRepository;
    private final EstimacionConsumoService estimacionConsumoService;

    public ReposicionServiceImpl(InsumoRepository insumoRepository,
                                 LoteRepository loteRepository,
                                 EstimacionConsumoService estimacionConsumoService) {
        this.insumoRepository = insumoRepository;
        this.loteRepository = loteRepository;
        this.estimacionConsumoService = estimacionConsumoService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SugerenciaReposicionResponse> calcularSugerencias(int ventanaConsumoDias) {
        validarVentana(ventanaConsumoDias);
        return insumoRepository.findAll().stream()
                .filter(Insumo::isActivo)
                .map(i -> buildSugerencia(i, ventanaConsumoDias))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SugerenciaReposicionResponse calcularSugerenciaPorInsumo(Long insumoId, int ventanaConsumoDias) {
        validarVentana(ventanaConsumoDias);
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + insumoId));
        return buildSugerencia(insumo, ventanaConsumoDias);
    }

    // ---- helpers ----

    private void validarVentana(int ventanaConsumoDias) {
        if (ventanaConsumoDias <= 0) {
            throw new IllegalArgumentException("ventanaConsumoDias debe ser > 0");
        }
    }

    private SugerenciaReposicionResponse buildSugerencia(Insumo insumo, int ventanaConsumoDias) {
        double stockActual = loteRepository.sumCantidadActualByInsumoId(insumo.getId());
        double consumoMedioDiario = estimacionConsumoService
                .calcularConsumoMedioDiario(insumo.getId(), ventanaConsumoDias);

        int leadTime = insumo.getLeadTimeDias();
        int cobertura = insumo.getDiasCobertura();

        double necesidad = consumoMedioDiario * (leadTime + cobertura);
        double cantidadSugerida = Math.max(0.0, necesidad - stockActual);

        double diasCoberturaRestante = (consumoMedioDiario > 0)
                ? stockActual / consumoMedioDiario
                : COBERTURA_INDETERMINADA;

        String nivelUrgencia = clasificarUrgencia(stockActual, consumoMedioDiario, leadTime, cobertura);

        return new SugerenciaReposicionResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                stockActual,
                consumoMedioDiario,
                leadTime,
                cobertura,
                cantidadSugerida,
                diasCoberturaRestante,
                nivelUrgencia);
    }

    private String clasificarUrgencia(double stockActual,
                                      double consumoMedioDiario,
                                      int leadTime,
                                      int cobertura) {
        if (consumoMedioDiario <= 0) {
            return NIVEL_OK;
        }
        double umbralUrgente  = consumoMedioDiario * leadTime;
        double umbralAtencion = consumoMedioDiario * (leadTime + cobertura / 2.0);
        if (stockActual < umbralUrgente)  return NIVEL_URGENTE;
        if (stockActual < umbralAtencion) return NIVEL_ATENCION;
        return NIVEL_OK;
    }
}
