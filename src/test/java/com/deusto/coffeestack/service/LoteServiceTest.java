package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.Proveedor;
import com.deusto.coffeestack.dto.LoteCreateRequest;
import com.deusto.coffeestack.dto.LoteResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import com.deusto.coffeestack.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class LoteServiceTest {

    @Mock LoteRepository loteRepository;
    @Mock InsumoRepository insumoRepository;
    @Mock ProveedorRepository proveedorRepository;

    @InjectMocks LoteServiceImpl service;

    private Insumo insumo;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        insumo = new Insumo();
        insumo.setId(1L);
        insumo.setNombre("Café Arabica");
        insumo.setUnidadMedida("kg");
        insumo.setActivo(true);

        proveedor = new Proveedor();
        proveedor.setId(5L);
        proveedor.setNombre("CaféCo");
        proveedor.setActivo(true);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private LoteCreateRequest buildRequest(Long insumoId, Long proveedorId,
                                           String numeroLote, double cantidad,
                                           LocalDate vencimiento) {
        LoteCreateRequest r = new LoteCreateRequest();
        r.setInsumoId(insumoId);
        r.setProveedorId(proveedorId);
        r.setNumeroLote(numeroLote);
        r.setCantidad(cantidad);
        r.setFechaVencimiento(vencimiento);
        return r;
    }

    private Lote savedLote(LoteCreateRequest req) {
        Lote l = new Lote();
        l.setId(10L);
        l.setInsumo(insumo);
        l.setProveedor(req.getProveedorId() != null ? proveedor : null);
        l.setNumeroLote(req.getNumeroLote());
        l.setCantidadInicial(req.getCantidad());
        l.setCantidadActual(req.getCantidad());
        l.setFechaVencimiento(req.getFechaVencimiento());
        return l;
    }

    // ── tests ──────────────────────────────────────────────────────────────────

    @Test
    void recibirLote_ok_persisteYDevuelveResponse() {
        LoteCreateRequest req = buildRequest(1L, null, "LC-2024-001", 50.0,
                LocalDate.of(2025, 6, 30));
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> savedLote(req));

        LoteResponse resp = service.recibirLote(req);

        assertNotNull(resp);
        assertEquals("LC-2024-001", resp.getNumeroLote());
        assertEquals(50.0, resp.getCantidadInicial(), 0.001);
        assertEquals(50.0, resp.getCantidadActual(), 0.001);
        assertEquals(LocalDate.of(2025, 6, 30), resp.getFechaVencimiento());
        assertNull(resp.getProveedorNombre());

        verify(loteRepository).save(any(Lote.class));
    }

    @Test
    void recibirLote_conProveedor_ok() {
        LoteCreateRequest req = buildRequest(1L, 5L, "LC-2024-002", 30.0, null);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(proveedorRepository.findById(5L)).thenReturn(Optional.of(proveedor));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> savedLote(req));

        LoteResponse resp = service.recibirLote(req);

        assertEquals("CaféCo", resp.getProveedorNombre());
        verify(proveedorRepository).findById(5L);
    }

    @Test
    void recibirLote_insumoNoExiste_throwsNotFoundException() {
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        LoteCreateRequest req = buildRequest(99L, null, "LC-X", 10.0, null);

        assertThrows(NotFoundException.class, () -> service.recibirLote(req));
        verify(loteRepository, never()).save(any());
    }

    @Test
    void recibirLote_insumoDesactivado_throwsIllegalArgument() {
        insumo.setActivo(false);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        LoteCreateRequest req = buildRequest(1L, null, "LC-X", 10.0, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.recibirLote(req));
        assertTrue(ex.getMessage().contains("desactivado"));
    }

    @Test
    void recibirLote_proveedorNoExiste_throwsNotFoundException() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        LoteCreateRequest req = buildRequest(1L, 99L, "LC-X", 10.0, null);

        assertThrows(NotFoundException.class, () -> service.recibirLote(req));
        verify(loteRepository, never()).save(any());
    }

    @Test
    void listarPorInsumo_insumoNoExiste_throwsNotFoundException() {
        when(insumoRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.listarPorInsumo(99L));
    }

    @Test
    void listarPorInsumo_ok_devuelveLista() {
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setInsumo(insumo);
        lote.setNumeroLote("LC-001");
        lote.setCantidadInicial(10.0);
        lote.setCantidadActual(10.0);

        when(insumoRepository.existsById(1L)).thenReturn(true);
        when(loteRepository.findByInsumoId(1L)).thenReturn(List.of(lote));

        List<LoteResponse> result = service.listarPorInsumo(1L);

        assertEquals(1, result.size());
        assertEquals("LC-001", result.get(0).getNumeroLote());
    }

    @Test
    void obtenerPorId_noEncontrado_throwsNotFoundException() {
        when(loteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.obtenerPorId(99L));
    }
}
