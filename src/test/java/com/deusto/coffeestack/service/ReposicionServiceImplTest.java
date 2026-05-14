package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.dto.SugerenciaReposicionResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReposicionServiceImpl — tests unitarios")
class ReposicionServiceImplTest {

    @Mock InsumoRepository insumoRepository;
    @Mock LoteRepository loteRepository;
    @Mock EstimacionConsumoService estimacionConsumoService;

    @InjectMocks ReposicionServiceImpl service;

    private Insumo insumo;

    @BeforeEach
    void setUp() {
        insumo = new Insumo();
        insumo.setId(1L);
        insumo.setNombre("Café");
        insumo.setUnidadMedida("kg");
        insumo.setActivo(true);
        insumo.setLeadTimeDias(3);
        insumo.setDiasCobertura(10);
    }

    // ── calcularSugerencias ───────────────────────────────────────────────────

    @Test
    @DisplayName("calcularSugerencias: ventana <= 0 lanza IllegalArgumentException")
    void calcularSugerencias_ventanaInvalida_lanzaExcepcion() {
        assertThatThrownBy(() -> service.calcularSugerencias(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ventanaConsumoDias");
    }

    @Test
    @DisplayName("calcularSugerencias: solo procesa insumos activos")
    void calcularSugerencias_soloInsumosActivos() {
        Insumo inactivo = new Insumo();
        inactivo.setId(2L);
        inactivo.setNombre("Azúcar");
        inactivo.setUnidadMedida("kg");
        inactivo.setActivo(false);
        inactivo.setLeadTimeDias(2);
        inactivo.setDiasCobertura(7);

        when(insumoRepository.findAll()).thenReturn(List.of(insumo, inactivo));
        when(loteRepository.sumCantidadActualByInsumoId(1L)).thenReturn(5.0);
        when(estimacionConsumoService.calcularConsumoMedioDiario(1L, 30)).thenReturn(1.0);

        List<SugerenciaReposicionResponse> result = service.calcularSugerencias(30);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInsumoId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("calcularSugerencias: sin consumo → nivelUrgencia OK y cobertura -1")
    void calcularSugerencias_sinConsumo_nivelOkYCoberturaIndeterminada() {
        when(insumoRepository.findAll()).thenReturn(List.of(insumo));
        when(loteRepository.sumCantidadActualByInsumoId(1L)).thenReturn(10.0);
        when(estimacionConsumoService.calcularConsumoMedioDiario(1L, 30)).thenReturn(0.0);

        SugerenciaReposicionResponse r = service.calcularSugerencias(30).get(0);

        assertThat(r.getNivelUrgencia()).isEqualTo("OK");
        assertThat(r.getDiasCoberturaRestante()).isEqualTo(-1.0);
        assertThat(r.getCantidadSugerida()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("calcularSugerencias: stock suficiente → cantidadSugerida 0, nivel OK")
    void calcularSugerencias_stockSuficiente_sinNecesidadDeReposicion() {
        when(insumoRepository.findAll()).thenReturn(List.of(insumo));
        // lead 3 + cobertura 10 = 13 días de necesidad; consumo 1 kg/día → necesita 13 kg
        // stock = 20 → suficiente
        when(loteRepository.sumCantidadActualByInsumoId(1L)).thenReturn(20.0);
        when(estimacionConsumoService.calcularConsumoMedioDiario(1L, 30)).thenReturn(1.0);

        SugerenciaReposicionResponse r = service.calcularSugerencias(30).get(0);

        assertThat(r.getCantidadSugerida()).isEqualTo(0.0);
        assertThat(r.getNivelUrgencia()).isEqualTo("OK");
        assertThat(r.getDiasCoberturaRestante()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("calcularSugerencias: stock bajo lead time → URGENTE con cantidad sugerida positiva")
    void calcularSugerencias_stockBajoLeadTime_nivelUrgente() {
        when(insumoRepository.findAll()).thenReturn(List.of(insumo));
        // consumo 2 kg/día, lead 3 → umbral urgente = 6 kg
        // stock = 4 → urgente, necesita 2*(3+10) - 4 = 22 kg
        when(loteRepository.sumCantidadActualByInsumoId(1L)).thenReturn(4.0);
        when(estimacionConsumoService.calcularConsumoMedioDiario(1L, 30)).thenReturn(2.0);

        SugerenciaReposicionResponse r = service.calcularSugerencias(30).get(0);

        assertThat(r.getNivelUrgencia()).isEqualTo("URGENTE");
        assertThat(r.getCantidadSugerida()).isEqualTo(22.0);
    }

    @Test
    @DisplayName("calcularSugerencias: stock entre umbrales → ATENCION")
    void calcularSugerencias_stockEntreUmbrales_nivelAtencion() {
        when(insumoRepository.findAll()).thenReturn(List.of(insumo));
        // consumo 2 kg/día, lead 3, cobertura 10
        // umbralUrgente = 6, umbralAtencion = 2*(3+5)=16 → stock 10 → ATENCION
        when(loteRepository.sumCantidadActualByInsumoId(1L)).thenReturn(10.0);
        when(estimacionConsumoService.calcularConsumoMedioDiario(1L, 30)).thenReturn(2.0);

        SugerenciaReposicionResponse r = service.calcularSugerencias(30).get(0);

        assertThat(r.getNivelUrgencia()).isEqualTo("ATENCION");
    }

    @Test
    @DisplayName("calcularSugerencias: lista vacía de insumos activos → lista vacía")
    void calcularSugerencias_sinInsumos_listaVacia() {
        when(insumoRepository.findAll()).thenReturn(List.of());

        List<SugerenciaReposicionResponse> result = service.calcularSugerencias(30);

        assertThat(result).isEmpty();
    }

    // ── calcularSugerenciaPorInsumo ───────────────────────────────────────────

    @Test
    @DisplayName("calcularSugerenciaPorInsumo: ventana <= 0 lanza IllegalArgumentException")
    void calcularSugerenciaPorInsumo_ventanaInvalida_lanzaExcepcion() {
        assertThatThrownBy(() -> service.calcularSugerenciaPorInsumo(1L, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("calcularSugerenciaPorInsumo: insumo inexistente → NotFoundException")
    void calcularSugerenciaPorInsumo_insumoNoExiste_lanzaNotFoundException() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calcularSugerenciaPorInsumo(99L, 30))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("calcularSugerenciaPorInsumo: insumo existente → sugerencia correcta")
    void calcularSugerenciaPorInsumo_insumoExistente_devuelveSugerencia() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(loteRepository.sumCantidadActualByInsumoId(1L)).thenReturn(5.0);
        when(estimacionConsumoService.calcularConsumoMedioDiario(1L, 7)).thenReturn(1.0);

        SugerenciaReposicionResponse r = service.calcularSugerenciaPorInsumo(1L, 7);

        assertThat(r.getInsumoId()).isEqualTo(1L);
        assertThat(r.getInsumoNombre()).isEqualTo("Café");
        assertThat(r.getUnidadMedida()).isEqualTo("kg");
        assertThat(r.getStockActual()).isEqualTo(5.0);
        assertThat(r.getConsumoMedioDiario()).isEqualTo(1.0);
        assertThat(r.getLeadTimeDias()).isEqualTo(3);
        assertThat(r.getDiasCobertura()).isEqualTo(10);
        // necesidad = 1*(3+10)=13, stock=5 → sugerida=8
        assertThat(r.getCantidadSugerida()).isEqualTo(8.0);
        // diasCoberturaRestante = 5/1 = 5
        assertThat(r.getDiasCoberturaRestante()).isEqualTo(5.0);
    }
}
