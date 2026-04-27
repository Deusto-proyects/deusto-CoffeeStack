package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.*;
import com.deusto.coffeestack.dto.*;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock VentaRepository ventaRepository;
    @Mock VentaLineaRepository ventaLineaRepository;
    @Mock ItemRepository itemRepository;
    @Mock RecetaItemRepository recetaItemRepository;
    @Mock LoteRepository loteRepository;
    @Mock MovimientoInventarioRepository movimientoRepository;

    @InjectMocks VentaServiceImpl service;

    private Item item;
    private Insumo insumo;
    private RecetaItem recetaItem;
    private Lote lote;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Café Latte");

        insumo = new Insumo();
        insumo.setId(10L);
        insumo.setNombre("Café en grano");
        insumo.setUnidadMedida("kg");

        recetaItem = new RecetaItem();
        recetaItem.setId(100L);
        recetaItem.setItem(item);
        recetaItem.setInsumo(insumo);
        recetaItem.setCantidad(0.02);

        lote = new Lote();
        lote.setId(50L);
        lote.setInsumo(insumo);
        lote.setNumeroLote("L-001");
        lote.setCantidadInicial(10.0);
        lote.setCantidadActual(10.0);
        lote.setFechaVencimiento(LocalDate.now().plusMonths(3));
    }

    private VentaRequest buildRequest(Long itemId, int cantidad) {
        VentaLineaRequest linea = new VentaLineaRequest();
        linea.setItemId(itemId);
        linea.setCantidadUnidades(cantidad);
        VentaRequest req = new VentaRequest();
        req.setLineas(List.of(linea));
        return req;
    }

    private void stubVentaSave() {
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });
        when(ventaLineaRepository.save(any(VentaLinea.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── registrarVenta con receta ─────────────────────────────────────────────

    @Test
    void registrarVenta_conReceta_descuentaInventarioYRegistraMovimiento() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of(recetaItem));
        when(loteRepository.sumCantidadActualByInsumoId(10L)).thenReturn(10.0);
        when(loteRepository.findByInsumoIdForUpdate(10L)).thenReturn(List.of(lote));
        when(loteRepository.save(any())).thenReturn(lote);
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubVentaSave();

        VentaResponse response = service.registrarVenta(buildRequest(1L, 2), "empleado1");

        assertEquals(1L, response.getId());
        assertEquals("empleado1", response.getUsuario());
        assertEquals(1, response.getLineas().size());

        // 2 unidades × 0.02 kg = 0.04 kg descontados
        assertEquals(10.0 - 0.04, lote.getCantidadActual(), 0.0001);

        ArgumentCaptor<MovimientoInventario> movCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(movCaptor.capture());
        assertEquals(TipoMovimiento.VENTA, movCaptor.getValue().getTipoMovimiento());
        assertEquals(0.04, movCaptor.getValue().getCantidad(), 0.0001);
        assertEquals("empleado1", movCaptor.getValue().getUsuario());
    }

    @Test
    void registrarVenta_sinReceta_registraSinDescontarInventario() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of());
        stubVentaSave();

        VentaResponse response = service.registrarVenta(buildRequest(1L, 3), "empleado1");

        assertEquals("empleado1", response.getUsuario());
        assertEquals(1, response.getLineas().size());
        assertEquals(1L, response.getLineas().get(0).getItemId());

        verify(loteRepository, never()).findByInsumoIdForUpdate(any());
        verify(loteRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarVenta_stockInsuficiente_throwsIllegalStateException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of(recetaItem));
        when(loteRepository.sumCantidadActualByInsumoId(10L)).thenReturn(0.01);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.registrarVenta(buildRequest(1L, 5), "empleado1"));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        assertTrue(ex.getMessage().contains("Café en grano"));
        verify(loteRepository, never()).save(any());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void registrarVenta_itemNoExiste_throwsNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.registrarVenta(buildRequest(99L, 1), "empleado1"));

        verify(ventaRepository, never()).save(any());
    }

    @Test
    void registrarVenta_consumeFIFO_primerLoteAgotadoAntesDeUsarSegundo() {
        Lote lote1 = new Lote();
        lote1.setId(51L);
        lote1.setInsumo(insumo);
        lote1.setNumeroLote("L-001");
        lote1.setCantidadInicial(0.5);
        lote1.setCantidadActual(0.5);
        lote1.setFechaVencimiento(LocalDate.now().plusDays(5));

        Lote lote2 = new Lote();
        lote2.setId(52L);
        lote2.setInsumo(insumo);
        lote2.setNumeroLote("L-002");
        lote2.setCantidadInicial(1.0);
        lote2.setCantidadActual(1.0);
        lote2.setFechaVencimiento(LocalDate.now().plusMonths(6));

        // necesita 0.02 * 30 = 0.6 kg (lote1 solo tiene 0.5)
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of(recetaItem));
        when(loteRepository.sumCantidadActualByInsumoId(10L)).thenReturn(1.5);
        when(loteRepository.findByInsumoIdForUpdate(10L)).thenReturn(List.of(lote1, lote2));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubVentaSave();

        service.registrarVenta(buildRequest(1L, 30), "empleado1");

        assertEquals(0.0, lote1.getCantidadActual(), 0.0001);
        assertEquals(1.0 - 0.1, lote2.getCantidadActual(), 0.0001);
        verify(movimientoRepository, times(2)).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarVenta_consumeFIFO_capturaMovimientosConCantidadesCorrectas() {
        // Preparamos dos lotes: el primero vence antes (FIFO) y solo tiene 0.5 kg
        Lote loteProximo = new Lote();
        loteProximo.setId(60L);
        loteProximo.setInsumo(insumo);
        loteProximo.setNumeroLote("L-FIFO-01");
        loteProximo.setCantidadInicial(0.5);
        loteProximo.setCantidadActual(0.5);
        loteProximo.setFechaVencimiento(LocalDate.now().plusDays(2)); // Vence muy pronto

        Lote loteLejano = new Lote();
        loteLejano.setId(61L);
        loteLejano.setInsumo(insumo);
        loteLejano.setNumeroLote("L-FIFO-02");
        loteLejano.setCantidadInicial(1.0);
        loteLejano.setCantidadActual(1.0);
        loteLejano.setFechaVencimiento(LocalDate.now().plusMonths(12)); // Vence en un año

        // Vendemos 50 unidades: 50 × 0.02 kg = 1.0 kg en total
        // → debe vaciarse loteProximo (0.5 kg) y descontarse 0.5 kg de loteLejano
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of(recetaItem));
        when(loteRepository.sumCantidadActualByInsumoId(10L)).thenReturn(1.5);
        when(loteRepository.findByInsumoIdForUpdate(10L)).thenReturn(List.of(loteProximo, loteLejano));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubVentaSave();

        service.registrarVenta(buildRequest(1L, 50), "empleado1");

        // Usamos ArgumentCaptor para capturar TODOS los movimientos guardados
        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository, times(2)).save(captor.capture());

        List<MovimientoInventario> movimientos = captor.getAllValues();

        // Verificamos el primer movimiento: debe corresponder al lote próximo a vencer
        MovimientoInventario mov1 = movimientos.get(0);
        assertEquals("L-FIFO-01", mov1.getLote().getNumeroLote(),
                "El primer movimiento debe apuntar al lote más próximo a vencer");
        assertEquals(0.5, mov1.getCantidad(), 0.0001,
                "Se debe haber descontado exactamente 0.5 kg del primer lote");
        assertEquals(TipoMovimiento.VENTA, mov1.getTipoMovimiento());

        // Verificamos el segundo movimiento: debe corresponder al lote lejano
        MovimientoInventario mov2 = movimientos.get(1);
        assertEquals("L-FIFO-02", mov2.getLote().getNumeroLote(),
                "El segundo movimiento debe apuntar al lote con más margen");
        assertEquals(0.5, mov2.getCantidad(), 0.0001,
                "Se debe haber descontado exactamente 0.5 kg del segundo lote");

        // Verificamos el stock residual de cada lote
        assertEquals(0.0, loteProximo.getCantidadActual(), 0.0001, "El lote próximo debe quedar vacío");
        assertEquals(0.5, loteLejano.getCantidadActual(), 0.0001, "El lote lejano debe tener 0.5 kg restantes");
    }

    @Test
    void registrarVenta_stockExacto_dejaCantidadEnCero() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of(recetaItem));
        when(loteRepository.sumCantidadActualByInsumoId(10L)).thenReturn(0.04);
        lote.setCantidadActual(0.04);
        when(loteRepository.findByInsumoIdForUpdate(10L)).thenReturn(List.of(lote));
        when(loteRepository.save(any())).thenReturn(lote);
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubVentaSave();

        service.registrarVenta(buildRequest(1L, 2), "empleado1");

        assertEquals(0.0, lote.getCantidadActual(), 0.0001);
    }
}
