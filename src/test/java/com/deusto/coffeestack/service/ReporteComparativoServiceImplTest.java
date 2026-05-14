package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.Granularidad;
import com.deusto.coffeestack.dto.ReporteComparativoResponse;
import com.deusto.coffeestack.dto.ReporteComparativoResponse.FilaInsumo;
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

/**
 * Tests unitarios de {@link ReporteComparativoServiceImpl} con Mockito.
 */
class ReporteComparativoServiceImplTest {

    private InsumoRepository insumoRepository;
    private MovimientoInventarioRepository movimientoRepository;
    private ReporteComparativoServiceImpl service;

    private Insumo cafe;
    private Insumo leche;
    private Lote loteCafe;
    private Lote loteLeche;

    @BeforeEach
    void setUp() {
        insumoRepository = mock(InsumoRepository.class);
        movimientoRepository = mock(MovimientoInventarioRepository.class);
        service = new ReporteComparativoServiceImpl(insumoRepository, movimientoRepository);

        cafe = new Insumo();
        cafe.setId(1L);
        cafe.setNombre("Café molido");
        cafe.setUnidadMedida("kg");
        cafe.setActivo(true);

        leche = new Insumo();
        leche.setId(2L);
        leche.setNombre("Leche entera");
        leche.setUnidadMedida("L");
        leche.setActivo(true);

        loteCafe = new Lote();
        loteCafe.setInsumo(cafe);
        loteCafe.setPrecioCompra(new BigDecimal("10.00"));

        loteLeche = new Lote();
        loteLeche.setInsumo(leche);
        loteLeche.setPrecioCompra(new BigDecimal("1.50"));
    }

    private MovimientoInventario mov(Long insumoId, LocalDateTime fecha, TipoMovimiento tipo,
                                     double cantidad, Lote lote) {
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
        assertThatThrownBy(() -> service.generar(List.of(1L), null, LocalDate.now(), Granularidad.DIA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lanzaSiDesdeMayorQueHasta() {
        assertThatThrownBy(() -> service.generar(
                List.of(1L),
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 1),
                Granularidad.DIA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── dos insumos seleccionados ─────────────────────────────────────────────

    @Test
    void conDosInsumosDevuelveDosFilas() {
        when(insumoRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(cafe, leche));
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(List.of(mov(1L, LocalDateTime.of(2026, 1, 2, 9, 0),
                        TipoMovimiento.VENTA, 5.0, loteCafe)));
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(2L), any(), any()))
                .thenReturn(List.of(mov(2L, LocalDateTime.of(2026, 1, 2, 9, 0),
                        TipoMovimiento.VENTA, 10.0, loteLeche)));

        ReporteComparativoResponse r = service.generar(
                List.of(1L, 2L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.DIA);

        assertThat(r.getInsumos()).hasSize(2);

        FilaInsumo filaCafe = r.getInsumos().stream()
                .filter(f -> f.getInsumoNombre().equals("Café molido")).findFirst().orElseThrow();
        assertThat(filaCafe.getTotalCantidad()).isEqualTo(5.0);
        assertThat(filaCafe.getCosteTotal()).isEqualByComparingTo("50.00");

        FilaInsumo filaLeche = r.getInsumos().stream()
                .filter(f -> f.getInsumoNombre().equals("Leche entera")).findFirst().orElseThrow();
        assertThat(filaLeche.getTotalCantidad()).isEqualTo(10.0);
        assertThat(filaLeche.getCosteTotal()).isEqualByComparingTo("15.00");
    }

    @Test
    void costeTotalGlobalEsSumaDeAmbasFilas() {
        when(insumoRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(cafe, leche));
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(List.of(mov(1L, LocalDateTime.of(2026, 1, 2, 9, 0),
                        TipoMovimiento.VENTA, 5.0, loteCafe)));   // 5 * 10 = 50
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(2L), any(), any()))
                .thenReturn(List.of(mov(2L, LocalDateTime.of(2026, 1, 2, 9, 0),
                        TipoMovimiento.VENTA, 10.0, loteLeche))); // 10 * 1.5 = 15

        ReporteComparativoResponse r = service.generar(
                List.of(1L, 2L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.DIA);

        assertThat(r.getCosteTotalGlobal()).isEqualByComparingTo("65.00");
    }

    @Test
    void serieInsumoTieneUnPuntoPorDiaCuandoGranularidadDia() {
        when(insumoRepository.findAllById(List.of(1L))).thenReturn(List.of(cafe));
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(List.of(
                        mov(1L, LocalDateTime.of(2026, 1, 2, 9, 0), TipoMovimiento.VENTA, 2.0, loteCafe),
                        mov(1L, LocalDateTime.of(2026, 1, 3, 9, 0), TipoMovimiento.VENTA, 3.0, loteCafe)
                ));

        ReporteComparativoResponse r = service.generar(
                List.of(1L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.DIA);

        FilaInsumo fila = r.getInsumos().get(0);
        assertThat(fila.getSerie()).hasSize(2);
        assertThat(fila.getSerie().get(0).getFecha()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(fila.getSerie().get(1).getFecha()).isEqualTo(LocalDate.of(2026, 1, 3));
    }

    @Test
    void conListaVaciaUsaTodosLosInsumosActivos() {
        Insumo inactivo = new Insumo();
        inactivo.setId(3L);
        inactivo.setNombre("Azúcar");
        inactivo.setUnidadMedida("kg");
        inactivo.setActivo(false);

        when(insumoRepository.findAll()).thenReturn(List.of(cafe, leche, inactivo));
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(any(), any(), any()))
                .thenReturn(List.of());

        ReporteComparativoResponse r = service.generar(
                List.of(),   // vacío → todos los activos
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.DIA);

        // Solo cafe y leche son activos
        assertThat(r.getInsumos()).hasSize(2);
        assertThat(r.getInsumos()).extracting(FilaInsumo::getInsumoNombre)
                .containsExactlyInAnyOrder("Café molido", "Leche entera");
    }

    @Test
    void conInsumoSinMovimientosDevuelveCantidadCero() {
        when(insumoRepository.findAllById(List.of(1L))).thenReturn(List.of(cafe));
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(List.of());

        ReporteComparativoResponse r = service.generar(
                List.of(1L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                Granularidad.DIA);

        assertThat(r.getInsumos()).hasSize(1);
        assertThat(r.getInsumos().get(0).getTotalCantidad()).isEqualTo(0.0);
        assertThat(r.getInsumos().get(0).getCosteTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.getInsumos().get(0).getSerie()).isEmpty();
    }

    @Test
    void granularidadMesAgrupaCorrectamente() {
        when(insumoRepository.findAllById(List.of(1L))).thenReturn(List.of(cafe));
        when(movimientoRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(List.of(
                        mov(1L, LocalDateTime.of(2026, 1, 10, 9, 0), TipoMovimiento.VENTA, 2.0, loteCafe),
                        mov(1L, LocalDateTime.of(2026, 1, 20, 9, 0), TipoMovimiento.VENTA, 3.0, loteCafe),
                        mov(1L, LocalDateTime.of(2026, 2, 5, 9, 0), TipoMovimiento.VENTA, 1.0, loteCafe)
                ));

        ReporteComparativoResponse r = service.generar(
                List.of(1L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 28),
                Granularidad.MES);

        FilaInsumo fila = r.getInsumos().get(0);
        assertThat(fila.getSerie()).hasSize(2);
        assertThat(fila.getSerie().get(0).getFecha()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(fila.getSerie().get(0).getCantidad()).isEqualTo(5.0);
        assertThat(fila.getSerie().get(1).getFecha()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(fila.getSerie().get(1).getCantidad()).isEqualTo(1.0);
    }
}
