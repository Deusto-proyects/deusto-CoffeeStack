package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.ReporteVentasDTO;
import com.deusto.coffeestack.dto.VentaRequest;
import com.deusto.coffeestack.dto.VentaResponse;
import com.deusto.coffeestack.service.CsvExportService;
import com.deusto.coffeestack.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VentaController — tests unitarios")
class VentaControllerTest {

    @Mock VentaService ventaService;
    @Mock CsvExportService csvExportService;

    @InjectMocks VentaController controller;

    private VentaResponse ventaResponse;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        ventaResponse = new VentaResponse(1L, "empleado1", LocalDateTime.now(), Collections.emptyList());

        userDetails = User.withUsername("empleado1")
                .password("pw")
                .roles("EMPLEADO")
                .build();
    }

    // ── registrar ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar: venta válida → 201 Created con Location header")
    void registrar_ventaValida_devuelve201() {
        VentaRequest request = new VentaRequest();
        when(ventaService.registrarVenta(request, "empleado1")).thenReturn(ventaResponse);

        ResponseEntity<VentaResponse> response = controller.registrar(request, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(ventaResponse);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/ventas/1");
    }

    @Test
    @DisplayName("registrar: userDetails null → usa 'sistema' como usuario")
    void registrar_sinUserDetails_usaSistema() {
        VentaRequest request = new VentaRequest();
        when(ventaService.registrarVenta(request, "sistema")).thenReturn(ventaResponse);

        ResponseEntity<VentaResponse> response = controller.registrar(request, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(ventaService).registrarVenta(request, "sistema");
    }

    @Test
    @DisplayName("registrar: delega el username correcto al servicio")
    void registrar_delegaUsernameAlServicio() {
        VentaRequest request = new VentaRequest();
        when(ventaService.registrarVenta(eq(request), eq("empleado1"))).thenReturn(ventaResponse);

        controller.registrar(request, userDetails);

        verify(ventaService, times(1)).registrarVenta(request, "empleado1");
    }

    // ── obtenerReporte ────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerReporte: devuelve lista del servicio")
    void obtenerReporte_devuelveListaDelServicio() {
        ReporteVentasDTO dto = new ReporteVentasDTO(LocalDate.parse("2024-01-01"), "Café", 5L);
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of(dto));

        List<ReporteVentasDTO> result = controller.obtenerReporte();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombreProducto()).isEqualTo("Café");
    }

    @Test
    @DisplayName("obtenerReporte: sin ventas → lista vacía")
    void obtenerReporte_sinVentas_listaVacia() {
        when(ventaService.obtenerReporteVentas()).thenReturn(Collections.emptyList());

        List<ReporteVentasDTO> result = controller.obtenerReporte();

        assertThat(result).isEmpty();
    }

    // ── descargarReporteCsv ───────────────────────────────────────────────────

    @Test
    @DisplayName("descargarReporteCsv: respuesta 200 con Content-Disposition attachment")
    void descargarReporteCsv_respuestaOkConAttachment() {
        when(ventaService.obtenerReporteVentas()).thenReturn(Collections.emptyList());
        when(csvExportService.ventasToCsv(anyList())).thenReturn("Fecha,Producto,Unidades\r\n");

        ResponseEntity<byte[]> response = controller.descargarReporteCsv();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(disposition).contains("attachment");
        assertThat(disposition).contains("reporte_ventas.csv");
    }

    @Test
    @DisplayName("descargarReporteCsv: Content-Type es text/csv")
    void descargarReporteCsv_contentTypeTextCsv() {
        when(ventaService.obtenerReporteVentas()).thenReturn(Collections.emptyList());
        when(csvExportService.ventasToCsv(anyList())).thenReturn("Fecha,Producto,Unidades\r\n");

        ResponseEntity<byte[]> response = controller.descargarReporteCsv();

        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).startsWith("text/csv");
    }

    @Test
    @DisplayName("descargarReporteCsv: body no es nulo y tiene contenido")
    void descargarReporteCsv_bodyNoNulo() {
        when(ventaService.obtenerReporteVentas()).thenReturn(Collections.emptyList());
        when(csvExportService.ventasToCsv(anyList())).thenReturn("contenido");

        ResponseEntity<byte[]> response = controller.descargarReporteCsv();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    // ── listar ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listar: devuelve lista del servicio")
    void listar_devuelveListaDelServicio() {
        when(ventaService.listarVentas()).thenReturn(List.of(ventaResponse));

        List<VentaResponse> result = controller.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    // ── obtener ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtener: id existente → VentaResponse correcto")
    void obtener_idExistente_devuelveVenta() {
        when(ventaService.obtenerVenta(1L)).thenReturn(ventaResponse);

        VentaResponse result = controller.obtener(1L);

        assertThat(result).isEqualTo(ventaResponse);
        verify(ventaService).obtenerVenta(1L);
    }
}
