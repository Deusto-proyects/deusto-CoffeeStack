package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ConsumoPorTipoDTO;
import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteConsumoServiceImplTest {

    private InsumoRepository insumoRepository;
    private MovimientoInventarioRepository movimientoRepository;
    private ReporteConsumoServiceImpl service;

    private Insumo insumo;
    private Lote loteConPrecio;
    private Lote loteSinPrecio;

    @BeforeEach
    void setUp() {
        insumoRepository = mock(InsumoRepository.class);
        movimientoRepository = mock(MovimientoInventarioRepository.class);
        service = new ReporteConsumoServiceImpl(insumoRepository, movimientoRepository);

        insumo = new Insumo();
        insumo.setId(1L);
        insumo.setNombre("Café molido");
        insumo.setUnidadMedida("kg");

        loteConPrecio = new Lote();
        loteConPrecio.setInsumo(insumo);
        loteConPrecio.setPrecioCompra(new BigDecimal("10.00"));

        loteSinPrecio = new Lote();
        loteSinPrecio.setInsumo(insumo);
        // precioCompra null

        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
    }

    private MovimientoInventario mov(LocalDateTime fecha, TipoMovimiento tipo, double cantidad, Lote lote) {
        MovimientoInventario m = new MovimientoInventario();
        m.setFechaHora(fecha);
        m.setTipoMovimiento(tipo);
        m.setCantidad(cantidad);
        m.setLote(lote);
        return m;
    }

    // ── validaciones ─────────────────────────────────────────────────────────

    @Test
    void lanzaSiFechasNulas() {
        assertThatThrownBy(() -> service.generar(1L, null, LocalDate.now(), Granularidad.DIA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lanzaSiDesdeMayorQueHasta() {
        assertThatThrownBy(() -> service.generar(1L,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 1),
                Granularidad.DIA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lanzaSiInsumoNoExiste() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.generar(99L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 10),
                Granularidad.DIA))
                .isInstanceOf(NotFoundException.class);
    }

    // ── cálculos ─────────────────────────────────────────────────────────────

    @Test
    void agregaCantidadYCostePorTipoTratandoPrecioNuloComoCero() {
        List<MovimientoInventario> movs = List.of(
                mov(LocalDateTime.of(2026, 1, 2, 9, 0), TipoMovimiento.VENTA, 5.0, loteConPrecio),  // coste 50
                mov(LocalDateTime.of(2026, 1, 2, 11, 0), TipoMovimiento.VENTA, 3.0, loteSinPrecio), // coste 0
                mov(LocalDateTime.of(2026, 1, 3, 8, 0), TipoMovimiento.MERMA, 2.0, loteConPrecio)   // coste 20
        );
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(movs);

        ReporteConsumoResponse r = service.generar(1L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.DIA);

        assertThat(r.getTotalCantidad()).isEqualTo(10.0);
        assertThat(r.getCosteTotal()).isEqualByComparingTo("70.00");

        ConsumoPorTipoDTO ventas = r.getDesglosePorTipo().stream()
                .filter(d -> d.getTipo() == TipoMovimiento.VENTA).findFirst().orElseThrow();
        assertThat(ventas.getCantidad()).isEqualTo(8.0);
        assertThat(ventas.getCoste()).isEqualByComparingTo("50.00");

        ConsumoPorTipoDTO mermas = r.getDesglosePorTipo().stream()
                .filter(d -> d.getTipo() == TipoMovimiento.MERMA).findFirst().orElseThrow();
        assertThat(mermas.getCantidad()).isEqualTo(2.0);
        assertThat(mermas.getCoste()).isEqualByComparingTo("20.00");
    }

    @Test
    void serieDiariaAgrupaPorFechaYDevuelveOrdenada() {
        List<MovimientoInventario> movs = List.of(
                mov(LocalDateTime.of(2026, 1, 3, 9, 0), TipoMovimiento.VENTA, 1.0, loteConPrecio),
                mov(LocalDateTime.of(2026, 1, 2, 9, 0), TipoMovimiento.VENTA, 2.0, loteConPrecio),
                mov(LocalDateTime.of(2026, 1, 2, 18, 0), TipoMovimiento.VENTA, 3.0, loteConPrecio)
        );
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(movs);

        ReporteConsumoResponse r = service.generar(1L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.DIA);

        assertThat(r.getSerie()).hasSize(2);
        assertThat(r.getSerie().get(0).getFecha()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(r.getSerie().get(0).getCantidad()).isEqualTo(5.0);
        assertThat(r.getSerie().get(1).getFecha()).isEqualTo(LocalDate.of(2026, 1, 3));
        assertThat(r.getSerie().get(1).getCantidad()).isEqualTo(1.0);
    }

    @Test
    void serieSemanalAgrupaPorLunesDeLaSemanaIso() {
        // miercoles 7 enero 2026 → lunes 5 ene; jueves 8 ene → lunes 5 ene; lunes 12 ene → lunes 12 ene
        List<MovimientoInventario> movs = List.of(
                mov(LocalDateTime.of(2026, 1, 7, 9, 0), TipoMovimiento.VENTA, 1.0, loteConPrecio),
                mov(LocalDateTime.of(2026, 1, 8, 9, 0), TipoMovimiento.VENTA, 2.0, loteConPrecio),
                mov(LocalDateTime.of(2026, 1, 12, 9, 0), TipoMovimiento.VENTA, 4.0, loteConPrecio)
        );
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(movs);

        ReporteConsumoResponse r = service.generar(1L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.SEMANA);

        assertThat(r.getSerie()).hasSize(2);
        assertThat(r.getSerie().get(0).getFecha()).isEqualTo(LocalDate.of(2026, 1, 5));
        assertThat(r.getSerie().get(0).getCantidad()).isEqualTo(3.0);
        assertThat(r.getSerie().get(1).getFecha()).isEqualTo(LocalDate.of(2026, 1, 12));
        assertThat(r.getSerie().get(1).getCantidad()).isEqualTo(4.0);
    }
}
