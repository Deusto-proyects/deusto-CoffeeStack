package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ConsumoPorTipoDTO;
import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.PuntoSerieDTO;
import com.deusto.coffeestack.dto.ReporteComparativoResponse;
import com.deusto.coffeestack.dto.ReporteComparativoResponse.FilaInsumo;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import com.deusto.coffeestack.service.CsvExportService;
import com.deusto.coffeestack.service.ReporteComparativoService;
import com.deusto.coffeestack.service.ReporteConsumoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReporteConsumoService reporteConsumoService;

    @MockBean
    private CsvExportService csvExportService;

    @MockBean
    private ReporteComparativoService comparativoService;

    @MockBean
    private com.deusto.coffeestack.security.JwtAuthFilter jwtAuthFilter;

    private ReporteConsumoResponse reporteEjemplo() {
        List<ConsumoPorTipoDTO> desglose = List.of(
                new ConsumoPorTipoDTO(TipoMovimiento.VENTA, 5.0, new BigDecimal("50.00"))
        );
        List<PuntoSerieDTO> serie = List.of(
                new PuntoSerieDTO(LocalDate.of(2026, 1, 2), 5.0, new BigDecimal("50.00"))
        );
        return new ReporteConsumoResponse(
                1L, "Café molido", "kg",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                Granularidad.DIA, 5.0, new BigDecimal("50.00"),
                desglose, serie
        );
    }

    // ── GET /api/reportes/consumo ─────────────────────────────────────────────

    @Test
    void consumoPorInsumo_retornaReporteJson() throws Exception {
        when(reporteConsumoService.generar(eq(1L), any(), any(), eq(Granularidad.DIA)))
                .thenReturn(reporteEjemplo());

        mockMvc.perform(get("/api/reportes/consumo")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31")
                        .param("granularidad", "DIA"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.insumoNombre").value("Café molido"))
                .andExpect(jsonPath("$.unidadMedida").value("kg"))
                .andExpect(jsonPath("$.totalCantidad").value(5.0))
                .andExpect(jsonPath("$.serie[0].fecha").value("2026-01-02"));
    }

    @Test
    void consumoPorInsumo_usaGranularidadPorDefectoDia() throws Exception {
        when(reporteConsumoService.generar(eq(1L), any(), any(), eq(Granularidad.DIA)))
                .thenReturn(reporteEjemplo());

        // Sin param granularidad → debe usar DIA por defecto
        mockMvc.perform(get("/api/reportes/consumo")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularidad").value("DIA"));
    }

    @Test
    void consumoPorInsumo_conGranularidadSemana() throws Exception {
        ReporteConsumoResponse reporteSemanal = new ReporteConsumoResponse(
                1L, "Café molido", "kg",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                Granularidad.SEMANA, 5.0, new BigDecimal("50.00"),
                List.of(), List.of()
        );
        when(reporteConsumoService.generar(eq(1L), any(), any(), eq(Granularidad.SEMANA)))
                .thenReturn(reporteSemanal);

        mockMvc.perform(get("/api/reportes/consumo")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31")
                        .param("granularidad", "SEMANA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularidad").value("SEMANA"));
    }

    // ── GET /api/reportes/consumo/csv ─────────────────────────────────────────

    @Test
    void descargarConsumoCsv_retornaCsvConHeaders() throws Exception {
        when(reporteConsumoService.generar(eq(1L), any(), any(), eq(Granularidad.DIA)))
                .thenReturn(reporteEjemplo());
        when(csvExportService.consumoToCsv(any())).thenReturn("Fecha,Cantidad\n2026-01-02,5.0\n");

        mockMvc.perform(get("/api/reportes/consumo/csv")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31")
                        .param("granularidad", "DIA"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("consumo_")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    void descargarConsumoCsv_nombreFicheroUsaNombreInsumo() throws Exception {
        when(reporteConsumoService.generar(eq(1L), any(), any(), eq(Granularidad.DIA)))
                .thenReturn(reporteEjemplo());
        when(csvExportService.consumoToCsv(any())).thenReturn("data");

        mockMvc.perform(get("/api/reportes/consumo/csv")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("Café_molido")));
    }

    // ── GET /api/reportes/consumo/comparativa ─────────────────────────────────

    private ReporteComparativoResponse comparativoEjemplo() {
        List<PuntoSerieDTO> serie = List.of(
                new PuntoSerieDTO(LocalDate.of(2026, 1, 2), 5.0, new BigDecimal("50.00"))
        );
        FilaInsumo fila = new FilaInsumo(1L, "Café molido", "kg", 5.0, new BigDecimal("50.00"), serie);
        return new ReporteComparativoResponse(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                Granularidad.DIA, new BigDecimal("50.00"),
                List.of(fila)
        );
    }

    @Test
    void consumoComparativo_retornaJsonConFilas() throws Exception {
        when(comparativoService.generar(any(), any(), any(), eq(Granularidad.DIA)))
                .thenReturn(comparativoEjemplo());

        mockMvc.perform(get("/api/reportes/consumo/comparativa")
                        .param("insumoIds", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31")
                        .param("granularidad", "DIA"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.insumos[0].insumoNombre").value("Café molido"))
                .andExpect(jsonPath("$.insumos[0].totalCantidad").value(5.0))
                .andExpect(jsonPath("$.costeTotalGlobal").value(50.0));
    }

    @Test
    void consumoComparativo_sinInsumoIds_acepta() throws Exception {
        when(comparativoService.generar(any(), any(), any(), eq(Granularidad.MES)))
                .thenReturn(new ReporteComparativoResponse(
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                        Granularidad.MES, BigDecimal.ZERO, List.of()
                ));

        mockMvc.perform(get("/api/reportes/consumo/comparativa")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31")
                        .param("granularidad", "MES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularidad").value("MES"))
                .andExpect(jsonPath("$.insumos").isArray());
    }
}
