package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.EstimacionConsumoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstimacionConsumoServiceTest {

    @Mock
    InsumoRepository insumoRepository;

    @Mock
    MovimientoInventarioRepository movimientoInventarioRepository;

    @InjectMocks
    EstimacionConsumoServiceImpl service;

    // ---- helpers ----

    private Insumo buildInsumo(Long id, String nombre) {
        Insumo i = new Insumo();
        i.setId(id);
        i.setNombre(nombre);
        i.setUnidadMedida("kg");
        i.setStockMinimoAlerta(0);
        return i;
    }

    private MovimientoInventario buildMovimiento(Insumo insumo, double cantidad, LocalDateTime fechaHora) {
        Lote lote = new Lote();
        lote.setInsumo(insumo);

        MovimientoInventario m = new MovimientoInventario();
        m.setLote(lote);
        m.setTipoMovimiento(TipoMovimiento.VENTA);
        m.setCantidad(cantidad);
        m.setMotivo("test");
        m.setUsuario("test");
        m.setFechaHora(fechaHora);
        return m;
    }

    // ---- tests ----

    @Test
    void calcular_devuelveMediaCorrecta_cuandoHayMovimientos() {
        Insumo cafe = buildInsumo(1L, "Café");
        LocalDateTime ahora = LocalDateTime.now();
        List<MovimientoInventario> movs = List.of(
                buildMovimiento(cafe, 10, ahora.minusDays(1)),
                buildMovimiento(cafe, 12, ahora.minusDays(3)),
                buildMovimiento(cafe, 8, ahora.minusDays(5))
        );

        when(insumoRepository.findById(1L)).thenReturn(Optional.of(cafe));
        when(movimientoInventarioRepository.findMovimientosSalidaByInsumoAndRango(eq(1L), any(), any()))
                .thenReturn(movs);

        EstimacionConsumoResponse resp = service.calcular(1L, 10, 7);

        assertEquals(30.0, resp.getConsumoTotalVentana(), 0.0001);
        assertEquals(3.0, resp.getConsumoMedioDiario(), 0.0001);
        assertEquals(21.0, resp.getConsumoProyectado(), 0.0001);
        assertEquals(10, resp.getVentanaDias());
        assertEquals(7, resp.getHorizonteDias());
        assertEquals("Café", resp.getInsumoNombre());
    }

    @Test
    void calcular_devuelveCero_cuandoNoHayMovimientos() {
        Insumo leche = buildInsumo(2L, "Leche");

        when(insumoRepository.findById(2L)).thenReturn(Optional.of(leche));
        when(movimientoInventarioRepository.findMovimientosSalidaByInsumoAndRango(eq(2L), any(), any()))
                .thenReturn(List.of());

        EstimacionConsumoResponse resp = service.calcular(2L, 30, 7);

        assertEquals(0.0, resp.getConsumoTotalVentana(), 0.0001);
        assertEquals(0.0, resp.getConsumoMedioDiario(), 0.0001);
        assertEquals(0.0, resp.getConsumoProyectado(), 0.0001);
        assertEquals(0, resp.getDiasConActividad());
    }

    @Test
    void calcular_lanzaNotFound_cuandoInsumoNoExiste() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.calcular(99L, 30, 7));
    }

    @Test
    void calcular_lanzaIllegalArgument_cuandoVentanaInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calcular(1L, 0, 7),
                "ventana=0 debe lanzar IllegalArgumentException");
    }

    @Test
    void calcular_diasConActividadCuentaDiasUnicos() {
        Insumo azucar = buildInsumo(3L, "Azúcar");
        LocalDateTime ahora = LocalDateTime.now();
        // 4 movimientos pero solo en 2 días distintos (hoy-1 y hoy-3)
        List<MovimientoInventario> movs = List.of(
                buildMovimiento(azucar, 5, ahora.minusDays(1).withHour(9)),
                buildMovimiento(azucar, 4, ahora.minusDays(1).withHour(15)),
                buildMovimiento(azucar, 3, ahora.minusDays(3).withHour(10)),
                buildMovimiento(azucar, 2, ahora.minusDays(3).withHour(18))
        );

        when(insumoRepository.findById(3L)).thenReturn(Optional.of(azucar));
        when(movimientoInventarioRepository.findMovimientosSalidaByInsumoAndRango(eq(3L), any(), any()))
                .thenReturn(movs);

        EstimacionConsumoResponse resp = service.calcular(3L, 10, 7);

        assertEquals(2, resp.getDiasConActividad(),
                "4 movimientos en 2 días distintos -> diasConActividad=2");
        assertEquals(14.0, resp.getConsumoTotalVentana(), 0.0001);
    }
}
