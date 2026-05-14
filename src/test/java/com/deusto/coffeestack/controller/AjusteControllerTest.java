package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.AjusteRequest;
import com.deusto.coffeestack.dto.MovimientoResponse;
import com.deusto.coffeestack.service.AjusteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AjusteController — tests unitarios")
class AjusteControllerTest {

    @Mock
    private AjusteService service;

    @InjectMocks
    private AjusteController controller;

    private MovimientoResponse movimientoResponse;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        movimientoResponse = mock(MovimientoResponse.class);
        lenient().when(movimientoResponse.getId()).thenReturn(1L);

        userDetails = User.withUsername("admin").password("pw").roles("ROOT").build();
    }

    @Test
    @DisplayName("registrar: ajuste válido → 201 Created con Location header")
    void registrar_ajusteValido_devuelve201() {
        AjusteRequest request = new AjusteRequest();
        when(service.registrarAjuste(any(AjusteRequest.class), eq("admin"))).thenReturn(movimientoResponse);

        ResponseEntity<MovimientoResponse> response = controller.registrar(request, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(movimientoResponse);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/ajustes/1");
        verify(service).registrarAjuste(request, "admin");
    }

    @Test
    @DisplayName("registrar: sin UserDetails → usa sistema")
    void registrar_sinUserDetails_usaSistema() {
        AjusteRequest request = new AjusteRequest();
        when(service.registrarAjuste(any(AjusteRequest.class), eq("sistema"))).thenReturn(movimientoResponse);

        ResponseEntity<MovimientoResponse> response = controller.registrar(request, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(service).registrarAjuste(request, "sistema");
    }

    @Test
    @DisplayName("listar: devuelve lista de movimientos")
    void listar_devuelveLista() {
        when(service.listarMovimientos()).thenReturn(List.of(movimientoResponse));

        List<MovimientoResponse> response = controller.listar();

        assertThat(response).hasSize(1);
        verify(service).listarMovimientos();
    }

    @Test
    @DisplayName("listarPorInsumo: devuelve lista de movimientos del insumo")
    void listarPorInsumo_devuelveLista() {
        when(service.listarMovimientosPorInsumo(1L)).thenReturn(List.of(movimientoResponse));

        List<MovimientoResponse> response = controller.listarPorInsumo(1L);

        assertThat(response).hasSize(1);
        verify(service).listarMovimientosPorInsumo(1L);
    }
}
