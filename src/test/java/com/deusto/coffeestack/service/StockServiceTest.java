package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.dto.CoberturaInsumoResponse;
import com.deusto.coffeestack.dto.EstimacionConsumoResponse;
import com.deusto.coffeestack.dto.StockInsumoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    InsumoRepository insumoRepository;

    @Mock
    LoteRepository loteRepository;

    @Mock
    EstimacionConsumoService estimacionConsumoService;

    @InjectMocks
    StockServiceImpl stockService;

    // ---- helpers ----

    private Insumo buildInsumo(Long id, String nombre, double umbral) {
        Insumo i = new Insumo();
        i.setId(id);
        i.setNombre(nombre);
        i.setUnidadMedida("kg");
        i.setStockMinimoAlerta(umbral);
        return i;
    }

    private Lote buildLote(Long id, Insumo insumo, String numero, double actual) {
        Lote l = new Lote();
        l.setId(id);
        l.setInsumo(insumo);
        l.setNumeroLote(numero);
        l.setCantidadInicial(actual);
        l.setCantidadActual(actual);
        return l;
    }

    // ---- tests ----

    @Test
    void getStock_sufficientStock_noRisk() {
        Insumo insumo = buildInsumo(1L, "Café", 5.0);
        Lote lote1 = buildLote(10L, insumo, "LC-001", 8.0);
        Lote lote2 = buildLote(11L, insumo, "LC-002", 4.0);

        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(loteRepository.findByInsumoId(1L)).thenReturn(List.of(lote1, lote2));

        StockInsumoResponse response = stockService.getStockDetalladoPorInsumo(1L);

        assertEquals(12.0, response.getCantidadTotal());
        assertFalse(response.isTieneRiesgoFaltante(),
                "Debe NO haber riesgo: 12 unidades >= umbral 5");
        assertEquals(2, response.getLotes().size());
        assertEquals(1L, response.getInsumo().getId());
    }

    @Test
    void getStock_belowThreshold_riskDetected() {
        Insumo insumo = buildInsumo(2L, "Leche", 10.0);
        Lote lote = buildLote(20L, insumo, "LL-001", 3.0);

        when(insumoRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(loteRepository.findByInsumoId(2L)).thenReturn(List.of(lote));

        StockInsumoResponse response = stockService.getStockDetalladoPorInsumo(2L);

        assertEquals(3.0, response.getCantidadTotal());
        assertTrue(response.isTieneRiesgoFaltante(),
                "Debe haber riesgo: 3 < umbral 10");
    }

    @Test
    void getStock_noLotes_riskDetected() {
        Insumo insumo = buildInsumo(3L, "Azúcar", 2.0);

        when(insumoRepository.findById(3L)).thenReturn(Optional.of(insumo));
        when(loteRepository.findByInsumoId(3L)).thenReturn(List.of());

        StockInsumoResponse response = stockService.getStockDetalladoPorInsumo(3L);

        assertEquals(0.0, response.getCantidadTotal());
        assertTrue(response.isTieneRiesgoFaltante(), "Sin lotes siempre hay riesgo");
        assertTrue(response.getLotes().isEmpty());
    }

    @Test
    void getStock_insumoNotFound_throwsNotFoundException() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.getStockDetalladoPorInsumo(99L));
    }

    @Test
    void getAllStock_returnsListForAllInsumos() {
        Insumo cafe = buildInsumo(1L, "Café", 5.0);
        Insumo leche = buildInsumo(2L, "Leche", 10.0);

        when(insumoRepository.findAll()).thenReturn(List.of(cafe, leche));
        when(loteRepository.findByInsumoId(1L)).thenReturn(List.of(buildLote(10L, cafe, "LC-001", 20.0)));
        when(loteRepository.findByInsumoId(2L)).thenReturn(List.of());

        List<StockInsumoResponse> all = stockService.getStockTodosInsumos();

        assertEquals(2, all.size());
        assertEquals("Café", all.get(0).getInsumo().getNombre());
        assertFalse(all.get(0).isTieneRiesgoFaltante());
        assertTrue(all.get(1).isTieneRiesgoFaltante());
    }
    // ---- tests: getCoberturaTodosInsumos ----

    /**
     * Helper: crea un EstimacionConsumoResponse con el consumo medio indicado.
     */
    private EstimacionConsumoResponse buildEstimacion(Insumo insumo, double consumoMedioDiario) {
        return new EstimacionConsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                30,                        // ventanaDias
                consumoMedioDiario * 30,   // consumoTotal
                consumoMedioDiario,
                15,                        // diasConActividad (irrelevante aqui)
                0,                         // horizonteDias
                0.0);                      // consumoProyectado
    }

    @Test
    void getCobertura_nivelOK_cuandoDiasCoberturaMayorOIgual7() {
        // 21 kg stock / 2 kg/dia = 10.5 dias -> OK
        Insumo cafe = buildInsumo(1L, "Cafe", 5.0);
        when(insumoRepository.findAll()).thenReturn(List.of(cafe));
        when(loteRepository.findByInsumoId(1L))
                .thenReturn(List.of(buildLote(10L, cafe, "LC-001", 21.0)));
        when(estimacionConsumoService.calcular(1L, 30, 0))
                .thenReturn(buildEstimacion(cafe, 2.0));

        List<CoberturaInsumoResponse> result = stockService.getCoberturaTodosInsumos(30);

        assertEquals(1, result.size());
        assertEquals("Cafe", result.get(0).getInsumoNombre());
        assertEquals("OK", result.get(0).getNivelRiesgo());
        assertEquals(10.5, result.get(0).getDiasCobertura(), 0.001);
    }

    @Test
    void getCobertura_nivelBAJO_cuandoDiasEntre3y6() {
        // 10 kg stock / 2 kg/dia = 5 dias -> BAJO
        Insumo leche = buildInsumo(2L, "Leche", 5.0);
        when(insumoRepository.findAll()).thenReturn(List.of(leche));
        when(loteRepository.findByInsumoId(2L))
                .thenReturn(List.of(buildLote(20L, leche, "LL-001", 10.0)));
        when(estimacionConsumoService.calcular(2L, 30, 0))
                .thenReturn(buildEstimacion(leche, 2.0));

        List<CoberturaInsumoResponse> result = stockService.getCoberturaTodosInsumos(30);

        assertEquals("BAJO", result.get(0).getNivelRiesgo());
        assertEquals(5.0, result.get(0).getDiasCobertura(), 0.001);
    }

    @Test
    void getCobertura_nivelCRITICO_cuandoDiasMenorQue3() {
        // 4 kg stock / 5 kg/dia = 0.8 dias -> CRITICO
        Insumo azucar = buildInsumo(3L, "Azucar", 2.0);
        when(insumoRepository.findAll()).thenReturn(List.of(azucar));
        when(loteRepository.findByInsumoId(3L))
                .thenReturn(List.of(buildLote(30L, azucar, "LA-001", 4.0)));
        when(estimacionConsumoService.calcular(3L, 30, 0))
                .thenReturn(buildEstimacion(azucar, 5.0));

        List<CoberturaInsumoResponse> result = stockService.getCoberturaTodosInsumos(30);

        assertEquals("CRITICO", result.get(0).getNivelRiesgo());
        assertEquals(0.8, result.get(0).getDiasCobertura(), 0.001);
    }

    @Test
    void getCobertura_sinConsumo_diasEsInfinito_nivelOK() {
        // Sin historial de ventas: consumoMedioDiario = 0 -> Infinity -> OK
        Insumo sal = buildInsumo(4L, "Sal", 1.0);
        when(insumoRepository.findAll()).thenReturn(List.of(sal));
        when(loteRepository.findByInsumoId(4L))
                .thenReturn(List.of(buildLote(40L, sal, "LS-001", 5.0)));
        when(estimacionConsumoService.calcular(4L, 30, 0))
                .thenReturn(buildEstimacion(sal, 0.0));

        List<CoberturaInsumoResponse> result = stockService.getCoberturaTodosInsumos(30);

        assertTrue(Double.isInfinite(result.get(0).getDiasCobertura()),
                "Sin consumo, los dias de cobertura deben ser Infinity");
        assertEquals("OK", result.get(0).getNivelRiesgo());
    }
}
