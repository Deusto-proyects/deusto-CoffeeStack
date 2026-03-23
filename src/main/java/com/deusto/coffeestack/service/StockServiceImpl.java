package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
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

    public StockServiceImpl(InsumoRepository insumoRepository, LoteRepository loteRepository) {
        this.insumoRepository = insumoRepository;
        this.loteRepository = loteRepository;
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
                        l.getFechaVencimiento()))
                .toList();

        InsumoResponse insumoResponse = new InsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                insumo.getStockMinimoAlerta());

        return new StockInsumoResponse(insumoResponse, cantidadTotal, tieneRiesgo, loteResponses);
    }
}
