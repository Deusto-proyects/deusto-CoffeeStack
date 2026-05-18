package com.deusto.coffeestack.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContextoNegocioServiceImpl — caché TTL del snapshot")
class ContextoNegocioServiceCacheTest {

    @Mock ReposicionService reposicionService;
    @Mock StockService stockService;
    @Mock VentaService ventaService;

    @Test
    @DisplayName("dos llamadas seguidas dentro del TTL: los servicios se invocan solo una vez")
    void cacheaResultadoDentroDelTtl() {
        AtomicReference<Instant> instante = new AtomicReference<>(Instant.parse("2026-05-18T12:00:00Z"));
        Clock clock = clockMutable(instante);

        ContextoNegocioServiceImpl service = new ContextoNegocioServiceImpl(
                reposicionService, stockService, ventaService, 300L, clock);

        stubsVacios();

        String s1 = service.generarSnapshot();
        // Avanzamos 1 minuto, sigue dentro del TTL de 5 min.
        instante.set(instante.get().plusSeconds(60));
        String s2 = service.generarSnapshot();

        assertThat(s2).isSameAs(s1);
        verify(stockService, times(1)).getStockTodosInsumos();
        verify(stockService, times(1)).getCoberturaTodosInsumos(30);
        verify(reposicionService, times(1)).calcularSugerencias(30);
        verify(ventaService, times(1)).obtenerReporteVentas();
    }

    @Test
    @DisplayName("tras expirar el TTL, se vuelve a calcular el snapshot")
    void recalculaTrasExpirarTtl() {
        AtomicReference<Instant> instante = new AtomicReference<>(Instant.parse("2026-05-18T12:00:00Z"));
        Clock clock = clockMutable(instante);

        ContextoNegocioServiceImpl service = new ContextoNegocioServiceImpl(
                reposicionService, stockService, ventaService, 300L, clock);

        stubsVacios();

        service.generarSnapshot();
        // Avanzamos 6 minutos, por encima del TTL de 5 min.
        instante.set(instante.get().plusSeconds(360));
        service.generarSnapshot();

        verify(stockService, times(2)).getStockTodosInsumos();
        verify(reposicionService, times(2)).calcularSugerencias(30);
        verify(ventaService, times(2)).obtenerReporteVentas();
    }

    @Test
    @DisplayName("con TTL = 0 se desactiva la caché (cada llamada recalcula)")
    void ttlCeroDesactivaCache() {
        ContextoNegocioServiceImpl service = new ContextoNegocioServiceImpl(
                reposicionService, stockService, ventaService, 0L, Clock.systemUTC());

        stubsVacios();

        service.generarSnapshot();
        service.generarSnapshot();
        service.generarSnapshot();

        verify(stockService, times(3)).getStockTodosInsumos();
        verify(reposicionService, times(3)).calcularSugerencias(30);
    }

    // ---- helpers ----

    private void stubsVacios() {
        when(stockService.getStockTodosInsumos()).thenReturn(List.of());
        when(stockService.getCoberturaTodosInsumos(30)).thenReturn(List.of());
        when(reposicionService.calcularSugerencias(30)).thenReturn(List.of());
        when(ventaService.obtenerReporteVentas()).thenReturn(List.of());
    }

    private static Clock clockMutable(AtomicReference<Instant> instante) {
        return new Clock() {
            @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId zone) { return this; }
            @Override public Instant instant() { return instante.get(); }
            @Override public long millis() { return instante.get().toEpochMilli(); }
        };
    }
}
