package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.ItemCreateRequest;
import com.deusto.coffeestack.dto.ItemResponse;
import com.deusto.coffeestack.dto.ItemUpdateRequest;
import com.deusto.coffeestack.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemController — tests unitarios")
class ItemControllerTest {

    @Mock
    private ItemService service;

    @InjectMocks
    private ItemController controller;

    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        itemResponse = new ItemResponse(
                1L,
                "Café con leche",
                "BEBIDA",
                Instant.now()
        );
    }

    @Test
    @DisplayName("list: sin items → página vacía")
    void list_sinItems_devuelvePaginaVacia() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(service.list(pageable)).thenReturn(page);

        Page<ItemResponse> response = controller.list(pageable);

        assertThat(response).isEmpty();
        verify(service).list(pageable);
    }

    @Test
    @DisplayName("list: con items → devuelve página con items")
    void list_conItems_devuelvePagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemResponse> page = new PageImpl<>(List.of(itemResponse), pageable, 1);
        when(service.list(pageable)).thenReturn(page);

        Page<ItemResponse> response = controller.list(pageable);

        assertThat(response).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        verify(service).list(pageable);
    }

    @Test
    @DisplayName("get: id existente → devuelve item")
    void get_idExistente_devuelveItem() {
        when(service.getById(1L)).thenReturn(itemResponse);

        ItemResponse response = controller.get(1L);

        assertThat(response).isEqualTo(itemResponse);
        verify(service).getById(1L);
    }

    @Test
    @DisplayName("create: solicitud válida → 201 Created con Location header")
    void create_solicitudValida_devuelve201() {
        ItemCreateRequest request = new ItemCreateRequest();
        request.setName("Café con leche");
        request.setDescription("BEBIDA");

        when(service.create(any(ItemCreateRequest.class))).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(itemResponse);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/items/1");
        verify(service).create(request);
    }

    @Test
    @DisplayName("update: solicitud válida → devuelve item actualizado")
    void update_solicitudValida_devuelveItem() {
        ItemUpdateRequest request = new ItemUpdateRequest();
        request.setName("Café con leche");
        request.setDescription("BEBIDA");

        when(service.update(eq(1L), any(ItemUpdateRequest.class))).thenReturn(itemResponse);

        ItemResponse response = controller.update(1L, request);

        assertThat(response).isEqualTo(itemResponse);
        verify(service).update(1L, request);
    }

    @Test
    @DisplayName("delete: id existente → 204 No Content")
    void delete_idExistente_devuelve204() {
        doNothing().when(service).delete(1L);

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).delete(1L);
    }
}
