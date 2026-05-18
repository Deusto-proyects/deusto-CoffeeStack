package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ReporteMotivoResponse;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AjusteServiceImpl.reportePorMotivo — tests unitarios")
class AjusteReporteMotivoServiceTest {

    @Mock LoteRepository loteRepository;
    @Mock InsumoRepository insumoRepository;
    @Mock MovimientoInventarioRepository movimientoRepository;

    @InjectMocks AjusteServiceImpl service;

    @Test
    @DisplayName("sin filtros: usa fechas sentinela y la query sin tipo")
    void sinFiltros_usaSentinelas() {
        when(movimientoRepository.agruparPorMotivo(any(), any()))
                .thenReturn(List.of(
                        new ReporteMotivoResponse("Caducidad", TipoMovimiento.MERMA, 5, 12.5,
                                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(1))));

        List<ReporteMotivoResponse> r = service.reportePorMotivo(null, null, null);

        assertThat(r).hasSize(1);
        assertThat(r.get(0).getMotivo()).isEqualTo("Caducidad");
        verify(movimientoRepository).agruparPorMotivo(any(), any());
        verify(movimientoRepository, never()).agruparPorMotivoYTipo(any(), any(), any());
    }

    @Test
    @DisplayName("con tipo: usa la query con tipo y propaga las fechas exactas")
    void conTipo_usaQueryEspecifica() {
        LocalDateTime desde = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 6, 30, 23, 59);
        when(movimientoRepository.agruparPorMotivoYTipo(TipoMovimiento.MERMA, desde, hasta))
                .thenReturn(List.of());

        service.reportePorMotivo(TipoMovimiento.MERMA, desde, hasta);

        verify(movimientoRepository).agruparPorMotivoYTipo(TipoMovimiento.MERMA, desde, hasta);
        verify(movimientoRepository, never()).agruparPorMotivo(any(), any());
    }

    @Test
    @DisplayName("desde > hasta lanza IllegalArgumentException y no toca el repositorio")
    void rangoInvertido_lanzaIllegalArgument() {
        LocalDateTime desde = LocalDateTime.of(2026, 6, 30, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 1, 1, 0, 0);

        assertThatThrownBy(() -> service.reportePorMotivo(null, desde, hasta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'desde'");

        verifyNoInteractions(movimientoRepository);
    }
}
