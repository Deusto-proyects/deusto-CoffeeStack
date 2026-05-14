package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.LoteCreateRequest;
import com.deusto.coffeestack.dto.LoteResponse;
import com.deusto.coffeestack.service.LoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoteController — tests unitarios")
class LoteControllerTest {

    @Mock
    private LoteService service;

    @InjectMocks
    private LoteController controller;

    private LoteResponse loteResponse;

    @BeforeEach
    void setUp() {
        loteResponse = mock(LoteResponse.class);
        lenient().when(loteResponse.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("recibir: lote válido → 201 Created")
    void recibir_loteValido_devuelve201() {
        LoteCreateRequest request = new LoteCreateRequest();
        when(service.recibirLote(any(LoteCreateRequest.class))).thenReturn(loteResponse);

        ResponseEntity<LoteResponse> response = controller.recibir(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(loteResponse);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/lotes/1");
        verify(service).recibirLote(request);
    }

    @Test
    @DisplayName("listarPorInsumo: devuelve lista")
    void listarPorInsumo_devuelveLista() {
        when(service.listarPorInsumo(1L)).thenReturn(List.of(loteResponse));

        List<LoteResponse> response = controller.listarPorInsumo(1L);

        assertThat(response).hasSize(1);
        verify(service).listarPorInsumo(1L);
    }

    @Test
    @DisplayName("obtener: id válido → devuelve lote")
    void obtener_idValido_devuelveLote() {
        when(service.obtenerPorId(1L)).thenReturn(loteResponse);

        LoteResponse response = controller.obtener(1L);

        assertThat(response).isEqualTo(loteResponse);
        verify(service).obtenerPorId(1L);
    }
}
