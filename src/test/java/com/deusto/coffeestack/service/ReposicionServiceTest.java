package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.dto.SugerenciaReposicionResponse;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReposicionServiceTest {

    @Mock
    InsumoRepository insumoRepository;

    @Mock
    LoteRepository loteRepository;

    @Mock
    EstimacionConsumoService estimacionConsumoService;

    @InjectMocks
    ReposicionServiceImpl service;

    // ---- helpers ----

    private Insumo buildInsumo(Long id, String nombre, int leadTime, int cobertura) {
        Insumo i = new Insumo();
        i.setId(id);
        i.setNombre(nombre);
        i.setUnidadMedida("kg");
        i.setStockMinimoAlerta(0);
        i.setActivo(true);
        i.setLeadTimeDias(leadTime);
        i.setDiasCobertura(cobertura);
        return i;
    }

    private void mockEscenario(Insumo insumo, double stock, double consumoMedio) {
        when(insumoRepository.findById(insumo.getId())).thenReturn(Optional.of(insumo));
        when(loteRepository.sumCantidadActualByInsumoId(insumo.getId())).thenReturn(stock);
        when(estimacionConsumoService.calcularConsumoMedioDiario(eq(insumo.getId()), anyInt()))
                .thenReturn(consumoMedio);
    }

    // ---- tests ----

    @Test
    void sugerenciaCero_cuandoStockSuperaConsumoEsperado() {
        // consumo=2/día, leadTime=7, cobertura=14 -> necesidad = 2*21 = 42
        // stock=1000 -> sugerencia = max(0, 42 - 1000) = 0
        Insumo cafe = buildInsumo(1L, "Café", 7, 14);
        mockEscenario(cafe, 1000, 2.0);

        SugerenciaReposicionResponse r = service.calcularSugerenciaPorInsumo(1L, 30);

        assertEquals(0.0, r.getCantidadSugerida(), 0.0001);
        assertEquals("OK", r.getNivelUrgencia());
    }

    @Test
    void sugerenciaPositiva_cuandoStockInsuficiente() {
        // consumo=5/día, leadTime=7, cobertura=14 -> necesidad = 5*21 = 105
        // stock=10 -> sugerencia = 95
        Insumo leche = buildInsumo(2L, "Leche", 7, 14);
        mockEscenario(leche, 10, 5.0);

        SugerenciaReposicionResponse r = service.calcularSugerenciaPorInsumo(2L, 30);

        assertEquals(95.0, r.getCantidadSugerida(), 0.0001);
    }

    @Test
    void nivelUrgenciaUrgente_cuandoStockMenorQueLeadTime() {
        // consumo=10/día, leadTime=7 -> umbral urgente = 70
        // stock=50 < 70 -> URGENTE
        Insumo azucar = buildInsumo(3L, "Azúcar", 7, 14);
        mockEscenario(azucar, 50, 10.0);

        SugerenciaReposicionResponse r = service.calcularSugerenciaPorInsumo(3L, 30);

        assertEquals("URGENTE", r.getNivelUrgencia());
    }

    @Test
    void nivelUrgenciaAtencion_cuandoStockEntreLeadTimeYMitadCobertura() {
        // consumo=10/día, leadTime=7, cobertura=14
        // umbralUrgente = 70, umbralAtencion = 10*(7+7) = 140
        // stock=120 -> 70 <= 120 < 140 -> ATENCION
        Insumo cacao = buildInsumo(4L, "Cacao", 7, 14);
        mockEscenario(cacao, 120, 10.0);

        SugerenciaReposicionResponse r = service.calcularSugerenciaPorInsumo(4L, 30);

        assertEquals("ATENCION", r.getNivelUrgencia());
    }

    @Test
    void nivelUrgenciaOk_cuandoConsumoCero() {
        Insumo te = buildInsumo(5L, "Té", 7, 14);
        mockEscenario(te, 5, 0.0);

        SugerenciaReposicionResponse r = service.calcularSugerenciaPorInsumo(5L, 30);

        assertEquals("OK", r.getNivelUrgencia());
        assertEquals(0.0, r.getCantidadSugerida(), 0.0001);
        assertEquals(-1.0, r.getDiasCoberturaRestante(), 0.0001,
                "Sin consumo -> diasCoberturaRestante=-1 (sentinela de indeterminado)");
    }

    @Test
    void lanzaIllegalArgument_cuandoVentanaInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularSugerencias(0),
                "ventana=0 debe lanzar IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularSugerenciaPorInsumo(1L, -1),
                "ventana negativa debe lanzar IllegalArgumentException");
    }

    @Test
    void lanzaNotFound_cuandoInsumoNoExiste() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.calcularSugerenciaPorInsumo(99L, 30));
    }

    @Test
    void calcularSugerencias_filtraInsumosInactivos() {
        Insumo activo = buildInsumo(1L, "Café", 7, 14);
        Insumo inactivo = buildInsumo(2L, "Antiguo", 7, 14);
        inactivo.setActivo(false);

        when(insumoRepository.findAll()).thenReturn(List.of(activo, inactivo));
        when(loteRepository.sumCantidadActualByInsumoId(1L)).thenReturn(10.0);
        when(estimacionConsumoService.calcularConsumoMedioDiario(eq(1L), anyInt())).thenReturn(2.0);

        List<SugerenciaReposicionResponse> lista = service.calcularSugerencias(30);

        assertEquals(1, lista.size(), "Solo el insumo activo debe aparecer");
        assertEquals("Café", lista.get(0).getInsumoNombre());
    }
}
