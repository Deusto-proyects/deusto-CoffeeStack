package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Item;
import com.deusto.coffeestack.domain.RecetaItem;
import com.deusto.coffeestack.dto.RecetaItemRequest;
import com.deusto.coffeestack.dto.RecetaRequest;
import com.deusto.coffeestack.dto.RecetaResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.ItemRepository;
import com.deusto.coffeestack.repository.RecetaItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    @Mock
    RecetaItemRepository recetaItemRepository;

    @Mock
    ItemRepository itemRepository;

    @Mock
    InsumoRepository insumoRepository;

    @InjectMocks
    RecetaServiceImpl service;

    // ---- helpers ----

    private Item buildItem(Long id, String name) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        return item;
    }

    private Insumo buildInsumo(Long id, String nombre, boolean activo) {
        Insumo insumo = new Insumo();
        insumo.setId(id);
        insumo.setNombre(nombre);
        insumo.setUnidadMedida("kg");
        insumo.setStockMinimoAlerta(1.0);
        insumo.setActivo(activo);
        return insumo;
    }

    private RecetaItemRequest buildIngrediente(Long insumoId, double cantidad) {
        RecetaItemRequest req = new RecetaItemRequest();
        req.setInsumoId(insumoId);
        req.setCantidad(cantidad);
        return req;
    }

    private RecetaItem buildRecetaItem(Long id, Item item, Insumo insumo, double cantidad) {
        RecetaItem ri = new RecetaItem();
        ri.setId(id);
        ri.setItem(item);
        ri.setInsumo(insumo);
        ri.setCantidad(cantidad);
        return ri;
    }

    // ---- definirReceta ----

    @Test
    void definirReceta_deberiaGuardarYDevolverReceta() {
        Item item = buildItem(1L, "Café Latte");
        Insumo insumo = buildInsumo(10L, "Café en grano", true);

        RecetaRequest request = new RecetaRequest();
        request.setIngredientes(List.of(buildIngrediente(10L, 0.02)));

        RecetaItem saved = buildRecetaItem(100L, item, insumo, 0.02);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(insumoRepository.findById(10L)).thenReturn(Optional.of(insumo));
        when(recetaItemRepository.save(any(RecetaItem.class))).thenReturn(saved);

        RecetaResponse response = service.definirReceta(1L, request);

        assertEquals(1L, response.itemId());
        assertEquals("Café Latte", response.itemName());
        assertEquals(1, response.ingredientes().size());
        assertEquals(10L, response.ingredientes().get(0).insumoId());
        assertEquals(0.02, response.ingredientes().get(0).cantidad());

        verify(recetaItemRepository).deleteByItemId(1L);
        verify(recetaItemRepository).save(any(RecetaItem.class));
    }

    @Test
    void definirReceta_itemNoExiste_lanzaNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        RecetaRequest request = new RecetaRequest();
        request.setIngredientes(List.of(buildIngrediente(10L, 0.02)));

        assertThrows(NotFoundException.class, () -> service.definirReceta(99L, request));
        verify(recetaItemRepository, never()).deleteByItemId(any());
    }

    @Test
    void definirReceta_insumoNoExiste_lanzaNotFoundException() {
        Item item = buildItem(1L, "Café Latte");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(insumoRepository.findById(10L)).thenReturn(Optional.empty());

        RecetaRequest request = new RecetaRequest();
        request.setIngredientes(List.of(buildIngrediente(10L, 0.02)));

        assertThrows(NotFoundException.class, () -> service.definirReceta(1L, request));
        verify(recetaItemRepository, never()).deleteByItemId(any());
    }

    @Test
    void definirReceta_insumoDesactivado_lanzaIllegalArgumentException() {
        Item item = buildItem(1L, "Café Latte");
        Insumo insumoDesactivado = buildInsumo(10L, "Leche", false);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(insumoRepository.findById(10L)).thenReturn(Optional.of(insumoDesactivado));

        RecetaRequest request = new RecetaRequest();
        request.setIngredientes(List.of(buildIngrediente(10L, 0.15)));

        assertThrows(IllegalArgumentException.class, () -> service.definirReceta(1L, request));
        verify(recetaItemRepository, never()).deleteByItemId(any());
    }

    @Test
    void definirReceta_insumosDuplicadosEnRequest_lanzaIllegalArgumentException() {
        Item item = buildItem(1L, "Café Latte");
        Insumo insumo = buildInsumo(10L, "Café en grano", true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(insumoRepository.findById(10L)).thenReturn(Optional.of(insumo));

        RecetaRequest request = new RecetaRequest();
        request.setIngredientes(List.of(
                buildIngrediente(10L, 0.02),
                buildIngrediente(10L, 0.01)   // duplicado
        ));

        assertThrows(IllegalArgumentException.class, () -> service.definirReceta(1L, request));
        verify(recetaItemRepository, never()).deleteByItemId(any());
    }

    @Test
    void definirReceta_conCantidadCeroONegativa_lanzaIllegalArgumentException() {
        Item item = buildItem(1L, "Café Latte");
        Insumo insumo = buildInsumo(10L, "Café en grano", true);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        RecetaRequest request = new RecetaRequest();
        // Intentamos pasar cantidad -0.5
        request.setIngredientes(List.of(buildIngrediente(10L, -0.5)));

        assertThrows(IllegalArgumentException.class, () -> service.definirReceta(1L, request));
        verify(recetaItemRepository, never()).deleteByItemId(any());
    }

    // ---- obtenerReceta ----

    @Test
    void obtenerReceta_devuelveIngredientes() {
        Item item = buildItem(1L, "Cappuccino");
        Insumo insumo = buildInsumo(10L, "Leche entera", true);
        RecetaItem ri = buildRecetaItem(100L, item, insumo, 0.15);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of(ri));

        RecetaResponse response = service.obtenerReceta(1L);

        assertEquals(1L, response.itemId());
        assertEquals(1, response.ingredientes().size());
        assertEquals("Leche entera", response.ingredientes().get(0).insumoNombre());
    }

    @Test
    void obtenerReceta_sinReceta_devuelveListaVacia() {
        Item item = buildItem(1L, "Cappuccino");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(recetaItemRepository.findByItemId(1L)).thenReturn(List.of());

        RecetaResponse response = service.obtenerReceta(1L);

        assertEquals(1L, response.itemId());
        assertTrue(response.ingredientes().isEmpty());
    }

    @Test
    void obtenerReceta_itemNoExiste_lanzaNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.obtenerReceta(99L));
    }

    // ---- eliminarReceta ----

    @Test
    void eliminarReceta_llamaDeleteByItemId() {
        when(itemRepository.existsById(1L)).thenReturn(true);

        service.eliminarReceta(1L);

        verify(recetaItemRepository).deleteByItemId(1L);
    }

    @Test
    void eliminarReceta_itemNoExiste_lanzaNotFoundException() {
        when(itemRepository.existsById(99L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.eliminarReceta(99L));
        verify(recetaItemRepository, never()).deleteByItemId(any());
    }
}
