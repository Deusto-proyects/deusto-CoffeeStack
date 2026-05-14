package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.EstimacionConsumoResponse;
import com.deusto.coffeestack.service.EstimacionConsumoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstimacionConsumoController — tests unitarios")
class EstimacionConsumoControllerTest {

    @Mock
    private EstimacionConsumoService service;

    @InjectMocks
    private EstimacionConsumoController controller;

    @Test
    @DisplayName("estimar: parametros válidos → 200 OK con respuesta")
    void estimar_parametrosValidos_devuelve200() {
        EstimacionConsumoResponse resp = mock(EstimacionConsumoResponse.class);
        when(service.calcular(1L, 30, 7)).thenReturn(resp);

        ResponseEntity<EstimacionConsumoResponse> response = controller.estimar(1L, 30, 7);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(resp);
        verify(service).calcular(1L, 30, 7);
    }
}
