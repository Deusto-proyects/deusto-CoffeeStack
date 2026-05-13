package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.dto.CoberturaInsumoResponse;
import com.deusto.coffeestack.dto.EstimacionConsumoResponse;
import com.deusto.coffeestack.dto.InsumoResponse;
import com.deusto.coffeestack.dto.LoteResponse;
import com.deusto.coffeestack.dto.StockInsumoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final InsumoRepository insumoRepository;
    private final LoteRepository loteRepository;
    private final EstimacionConsumoService estimacionConsumoService;

    public StockServiceImpl(InsumoRepository insumoRepository,
                            LoteRepository loteRepository,
                            EstimacionConsumoService estimacionConsumoService) {
        this.insumoRepository = insumoRepository;
        this.loteRepository = loteRepository;
        this.estimacionConsumoService = estimacionConsumoService;
    }

    @Override
    @Transactional(readOnly = true)
    public StockInsumoResponse getStockDetalladoPorInsumo(Long insumoId) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + insumoId));
        return buildResponse(insumo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockInsumoResponse> getStockTodosInsumos() {
        return insumoRepository.findAll().stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoberturaInsumoResponse> getCoberturaTodosInsumos(int ventanaDias) {
        return insumoRepository.findAll().stream()
                .map(insumo -> buildCoberturaResponse(insumo, ventanaDias))
                .sorted(java.util.Comparator.comparingDouble(r ->
                        Double.isInfinite(r.getDiasCobertura()) ? Double.MAX_VALUE : r.getDiasCobertura()))
                .toList();
    }

    // ---- private helpers ----

    private StockInsumoResponse buildResponse(Insumo insumo) {
        List<Lote> lotes = loteRepository.findByInsumoId(insumo.getId());
        double cantidadTotal = lotes.stream()
                .mapToDouble(Lote::getCantidadActual)
                .sum();

        boolean tieneRiesgo = cantidadTotal < insumo.getStockMinimoAlerta();

        List<LoteResponse> loteResponses = lotes.stream()
                .map(l -> new LoteResponse(
                        l.getId(),
                        l.getNumeroLote(),
                        l.getCantidadInicial(),
                        l.getCantidadActual(),
                        l.getFechaVencimiento(),
                        l.getProveedor() != null ? l.getProveedor().getNombre() : null,
                        l.getPrecioCompra()))
                .toList();

        InsumoResponse insumoResponse = new InsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                insumo.getStockMinimoAlerta(),
                insumo.isActivo(),
                insumo.getLeadTimeDias(),
                insumo.getDiasCobertura());

        return new StockInsumoResponse(insumoResponse, cantidadTotal, tieneRiesgo, loteResponses);
    }

    private CoberturaInsumoResponse buildCoberturaResponse(Insumo insumo, int ventanaDias) {
        // Stock actual = suma de cantidadActual de todos los lotes
        List<Lote> lotes = loteRepository.findByInsumoId(insumo.getId());
        double stockActual = lotes.stream()
                .mapToDouble(Lote::getCantidadActual)
                .sum();

        // Consumo medio diario sobre la ventana indicada (horizonte=0, solo nos interesa la media)
        EstimacionConsumoResponse estimacion =
                estimacionConsumoService.calcular(insumo.getId(), ventanaDias, 0);
        double consumoMedioDiario = estimacion.getConsumoMedioDiario();

        // Días de cobertura
        double diasCobertura = (consumoMedioDiario > 0)
                ? stockActual / consumoMedioDiario
                : Double.POSITIVE_INFINITY;

        // Semáforo de riesgo
        String nivelRiesgo;
        if (Double.isInfinite(diasCobertura)) {
            nivelRiesgo = "OK";
        } else if (diasCobertura < 3) {
            nivelRiesgo = "CRITICO";
        } else if (diasCobertura < 7) {
            nivelRiesgo = "BAJO";
        } else {
            nivelRiesgo = "OK";
        }

        return new CoberturaInsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                stockActual,
                consumoMedioDiario,
                diasCobertura,
                nivelRiesgo,
                ventanaDias);
    }
}
