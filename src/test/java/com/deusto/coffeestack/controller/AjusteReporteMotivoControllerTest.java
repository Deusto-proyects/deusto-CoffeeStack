package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ReporteMotivoResponse;
import com.deusto.coffeestack.service.AjusteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AjusteController.reportePorMotivo — tests unitarios")
class AjusteReporteMotivoControllerTest {

    @Mock AjusteService ajusteService;

    @InjectMocks AjusteController controller;

    @Test
    @DisplayName("sin parámetros: pasa nulls al servicio y devuelve la lista tal cual")
    void sinParametros_pasaNulls() {
        ReporteMotivoResponse fila = new ReporteMotivoResponse(
                "Caducidad lote leche", TipoMovimiento.MERMA, 3, 4.2,
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
        when(ajusteService.reportePorMotivo(null, null, null)).thenReturn(List.of(fila));

        List<ReporteMotivoResponse> resp = controller.reportePorMotivo(null, null, null);

        assertThat(resp).hasSize(1).containsExactly(fila);
    }

    @Test
    @DisplayName("desde/hasta se convierten a inicio y fin de día respectivamente")
    void fechas_seConviertenAInicioYFinDeDia() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 6, 30);
        when(ajusteService.reportePorMotivo(eq(TipoMovimiento.MERMA), any(), any()))
                .thenReturn(List.of());

        controller.reportePorMotivo(TipoMovimiento.MERMA, desde, hasta);

        ArgumentCaptor<LocalDateTime> desdeCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hastaCap = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(ajusteService).reportePorMotivo(eq(TipoMovimiento.MERMA), desdeCap.capture(), hastaCap.capture());

        assertThat(desdeCap.getValue()).isEqualTo(desde.atStartOfDay());
        assertThat(hastaCap.getValue()).isEqualTo(hasta.atTime(LocalTime.MAX));
    }
}
