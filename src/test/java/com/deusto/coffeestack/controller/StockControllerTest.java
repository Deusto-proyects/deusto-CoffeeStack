package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.CoberturaInsumoResponse;
import com.deusto.coffeestack.dto.StockInsumoResponse;
import com.deusto.coffeestack.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockController — tests unitarios")
class StockControllerTest {

    @Mock
    private StockService service;

    @InjectMocks
    private StockController controller;

    @Test
    @DisplayName("getAllStock: devuelve lista del servicio")
    void getAllStock_devuelveLista() {
        StockInsumoResponse resp = mock(StockInsumoResponse.class);
        when(service.getStockTodosInsumos()).thenReturn(List.of(resp));

        List<StockInsumoResponse> response = controller.getAllStock();

        assertThat(response).hasSize(1);
        verify(service).getStockTodosInsumos();
    }

    @Test
    @DisplayName("getStockByInsumo: devuelve detalle del servicio")
    void getStockByInsumo_devuelveDetalle() {
        StockInsumoResponse resp = mock(StockInsumoResponse.class);
        when(service.getStockDetalladoPorInsumo(1L)).thenReturn(resp);

        StockInsumoResponse response = controller.getStockByInsumo(1L);

        assertThat(response).isEqualTo(resp);
        verify(service).getStockDetalladoPorInsumo(1L);
    }

    @Test
    @DisplayName("getCobertura: devuelve lista de cobertura")
    void getCobertura_devuelveLista() {
        CoberturaInsumoResponse resp = mock(CoberturaInsumoResponse.class);
        when(service.getCoberturaTodosInsumos(30)).thenReturn(List.of(resp));

        List<CoberturaInsumoResponse> response = controller.getCobertura(30);

        assertThat(response).hasSize(1);
        verify(service).getCoberturaTodosInsumos(30);
    }
}
