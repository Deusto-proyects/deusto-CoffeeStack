package com.deusto.coffeestack.dto;

import com.deusto.coffeestack.exception.ApiError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DtoTest — cobertura para DTOs y Excepciones")
class DtoTest {

    @Test
    @DisplayName("SugerenciaReposicionResponse getters y constructor")
    void sugerenciaReposicionResponse() {
        SugerenciaReposicionResponse dto = new SugerenciaReposicionResponse(
                1L, "Café", "kg", 10.0, 2.0, 3, 5, 20.0, 5.0, "OK"
        );

        assertThat(dto.getInsumoId()).isEqualTo(1L);
        assertThat(dto.getInsumoNombre()).isEqualTo("Café");
        assertThat(dto.getUnidadMedida()).isEqualTo("kg");
        assertThat(dto.getStockActual()).isEqualTo(10.0);
        assertThat(dto.getConsumoMedioDiario()).isEqualTo(2.0);
        assertThat(dto.getLeadTimeDias()).isEqualTo(3);
        assertThat(dto.getDiasCobertura()).isEqualTo(5);
        assertThat(dto.getCantidadSugerida()).isEqualTo(20.0);
        assertThat(dto.getDiasCoberturaRestante()).isEqualTo(5.0);
        assertThat(dto.getNivelUrgencia()).isEqualTo("OK");
    }

    @Test
    @DisplayName("ReporteVentasDTO getters y constructores")
    void reporteVentasDTO() {
        LocalDate date = LocalDate.now();
        ReporteVentasDTO dto = new ReporteVentasDTO(date, "Café", 100L);

        assertThat(dto.getFecha()).isEqualTo(date);
        assertThat(dto.getNombreProducto()).isEqualTo("Café");
        assertThat(dto.getCantidadTotal()).isEqualTo(100L);

        ReporteVentasDTO dto2 = new ReporteVentasDTO();
        dto2.setFecha(date);
        dto2.setNombreProducto("Té");
        dto2.setCantidadTotal(50L);

        assertThat(dto2.getFecha()).isEqualTo(date);
        assertThat(dto2.getNombreProducto()).isEqualTo("Té");
        assertThat(dto2.getCantidadTotal()).isEqualTo(50L);
    }

    @Test
    @DisplayName("ApiError getters y constructor")
    void apiError() {
        ApiError error = new ApiError("Error message");

        assertThat(error.getMessage()).isEqualTo("Error message");
        assertThat(error.getTimestamp()).isNotNull();
    }
}
