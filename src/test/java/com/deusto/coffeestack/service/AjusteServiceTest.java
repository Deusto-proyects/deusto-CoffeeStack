package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.AjusteRequest;
import com.deusto.coffeestack.dto.MovimientoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AjusteServiceTest {

    @Mock LoteRepository loteRepository;
    @Mock InsumoRepository insumoRepository;
    @Mock MovimientoInventarioRepository movimientoRepository;

    @InjectMocks AjusteServiceImpl ajusteService;

    private Insumo insumo;
    private Lote lote;

    @BeforeEach
    void setUp() {
        insumo = new Insumo();
        insumo.setId(1L);
        insumo.setNombre("Café");
        insumo.setUnidadMedida("kg");

        lote = new Lote();
        lote.setId(10L);
        lote.setInsumo(insumo);
        lote.setNumeroLote("LC-001");
        lote.setCantidadInicial(20.0);
        lote.setCantidadActual(15.0);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private AjusteRequest buildRequest(TipoMovimiento tipo, double cantidad, String motivo) {
        AjusteRequest r = new AjusteRequest();
        r.setLoteId(10L);
        r.setTipoMovimiento(tipo);
        r.setCantidad(cantidad);
        r.setMotivo(motivo);
        return r;
    }

    private MovimientoInventario stubSave() {
        when(movimientoRepository.save(any())).thenAnswer(inv -> {
            MovimientoInventario m = inv.getArgument(0);
            m.setId(100L);
            if (m.getFechaHora() == null) m.setFechaHora(LocalDateTime.now());
            return m;
        });
        return null; // unused return
    }

    // ── tests ──────────────────────────────────────────────────────────────────

    @Test
    void registrarMerma_ok_restaCantidadYPersiste() {
        when(loteRepository.findById(10L)).thenReturn(Optional.of(lote));
        stubSave();

        MovimientoResponse resp = ajusteService.registrarAjuste(
                buildRequest(TipoMovimiento.MERMA, 3.0, "Producto caducado"), "admin");

        assertEquals(12.0, lote.getCantidadActual(), 0.001);
        assertEquals(TipoMovimiento.MERMA, resp.getTipoMovimiento());
        assertEquals(3.0, resp.getCantidad());
        assertEquals("admin", resp.getUsuario());

        verify(loteRepository).save(lote);
        verify(movimientoRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarRotura_ok_restaCantidad() {
        when(loteRepository.findById(10L)).thenReturn(Optional.of(lote));
        stubSave();

        ajusteService.registrarAjuste(
                buildRequest(TipoMovimiento.ROTURA, 5.0, "Botella rota en almacén"), "admin");

        assertEquals(10.0, lote.getCantidadActual(), 0.001);
    }

    @Test
    void registrarAjustePositivo_ok_sumaCantidad() {
        when(loteRepository.findById(10L)).thenReturn(Optional.of(lote));
        stubSave();

        ajusteService.registrarAjuste(
                buildRequest(TipoMovimiento.AJUSTE_POSITIVO, 2.5, "Sobrante detectado en recuento"), "usuario1");

        assertEquals(17.5, lote.getCantidadActual(), 0.001);
    }

    @Test
    void registrarAjusteNegativo_ok_restaCantidad() {
        when(loteRepository.findById(10L)).thenReturn(Optional.of(lote));
        stubSave();

        ajusteService.registrarAjuste(
                buildRequest(TipoMovimiento.AJUSTE_NEGATIVO, 1.0, "Faltante en recuento físico"), "admin");

        assertEquals(14.0, lote.getCantidadActual(), 0.001);
    }

    @Test
    void registrarMerma_superaStock_throwsIllegalArgument() {
        when(loteRepository.findById(10L)).thenReturn(Optional.of(lote));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ajusteService.registrarAjuste(
                        buildRequest(TipoMovimiento.MERMA, 999.0, "Pérdida total"), "admin"));

        assertTrue(ex.getMessage().contains("supera el stock actual"));
        verify(loteRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarAjuste_loteNoExiste_throwsNotFoundException() {
        when(loteRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ajusteService.registrarAjuste(
                        buildRequest(TipoMovimiento.MERMA, 1.0, "Prueba"), "admin"));
    }

    @Test
    void registrarMerma_cantidadExacta_dejaLoteEnCero() {
        when(loteRepository.findById(10L)).thenReturn(Optional.of(lote));
        stubSave();

        ajusteService.registrarAjuste(
                buildRequest(TipoMovimiento.MERMA, 15.0, "Lote agotado por merma total"), "admin");

        assertEquals(0.0, lote.getCantidadActual(), 0.001);
    }

    @Test
    void listarMovimientosPorInsumo_insumoNoExiste_throwsNotFoundException() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> ajusteService.listarMovimientosPorInsumo(99L));
    }

    @Test
    void listarMovimientos_returnsAll() {
        when(movimientoRepository.findAll()).thenReturn(List.of());

        List<MovimientoResponse> result = ajusteService.listarMovimientos();

        assertTrue(result.isEmpty());
        verify(movimientoRepository).findAll();
    }

    @Test
    void registrarMerma_persiste_motivoCorrectamente() {
        when(loteRepository.findById(10L)).thenReturn(Optional.of(lote));
        stubSave();

        String motivoEsperado = "Producto caducado el día de hoy";
        ajusteService.registrarAjuste(
                buildRequest(TipoMovimiento.MERMA, 1.0, motivoEsperado), "admin");

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(captor.capture());
        assertEquals(motivoEsperado, captor.getValue().getMotivo());
    }
}
