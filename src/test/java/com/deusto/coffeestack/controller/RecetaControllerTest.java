package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.RecetaRequest;
import com.deusto.coffeestack.dto.RecetaResponse;
import com.deusto.coffeestack.service.RecetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecetaController — tests unitarios")
class RecetaControllerTest {

    @Mock
    private RecetaService service;

    @InjectMocks
    private RecetaController controller;

    private RecetaResponse recetaResponse;

    @BeforeEach
    void setUp() {
        recetaResponse = mock(RecetaResponse.class);
    }

    @Test
    @DisplayName("definirReceta: solicitud válida → devuelve receta")
    void definirReceta_solicitudValida_devuelveReceta() {
        RecetaRequest request = new RecetaRequest();
        when(service.definirReceta(eq(1L), any(RecetaRequest.class))).thenReturn(recetaResponse);

        RecetaResponse response = controller.definirReceta(1L, request);

        assertThat(response).isEqualTo(recetaResponse);
        verify(service).definirReceta(1L, request);
    }

    @Test
    @DisplayName("obtenerReceta: id válido → devuelve receta")
    void obtenerReceta_idValido_devuelveReceta() {
        when(service.obtenerReceta(1L)).thenReturn(recetaResponse);

        RecetaResponse response = controller.obtenerReceta(1L);

        assertThat(response).isEqualTo(recetaResponse);
        verify(service).obtenerReceta(1L);
    }

    @Test
    @DisplayName("eliminarReceta: id válido → 204 No Content")
    void eliminarReceta_idValido_devuelve204() {
        doNothing().when(service).eliminarReceta(1L);

        ResponseEntity<Void> response = controller.eliminarReceta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).eliminarReceta(1L);
    }
}
