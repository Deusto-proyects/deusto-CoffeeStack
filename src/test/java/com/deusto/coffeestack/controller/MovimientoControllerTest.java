package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.MovimientoResponse;
import com.deusto.coffeestack.service.AjusteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimientoController — tests unitarios")
class MovimientoControllerTest {

    @Mock
    private AjusteService service;

    @InjectMocks
    private MovimientoController controller;

    @Test
    @DisplayName("historial: con todos los filtros → delega al servicio con LocalDateTime")
    void historial_conFiltros_delegaAlServicio() {
        LocalDate desde = LocalDate.of(2024, 1, 1);
        LocalDate hasta = LocalDate.of(2024, 1, 31);
        LocalDateTime desdeDt = desde.atStartOfDay();
        LocalDateTime hastaDt = hasta.atTime(LocalTime.MAX);

        MovimientoResponse mockResp = mock(MovimientoResponse.class);
        when(service.listarMovimientosFiltrados(1L, TipoMovimiento.VENTA, desdeDt, hastaDt))
                .thenReturn(List.of(mockResp));

        List<MovimientoResponse> response = controller.historial(1L, TipoMovimiento.VENTA, desde, hasta);

        assertThat(response).hasSize(1);
        verify(service).listarMovimientosFiltrados(1L, TipoMovimiento.VENTA, desdeDt, hastaDt);
    }

    @Test
    @DisplayName("historial: sin fechas → delega con null")
    void historial_sinFechas_delegaConNull() {
        when(service.listarMovimientosFiltrados(null, null, null, null))
                .thenReturn(List.of());

        List<MovimientoResponse> response = controller.historial(null, null, null, null);

        assertThat(response).isEmpty();
        verify(service).listarMovimientosFiltrados(null, null, null, null);
    }
}
