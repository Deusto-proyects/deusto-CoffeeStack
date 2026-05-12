package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.InsumoCreateRequest;
import com.deusto.coffeestack.dto.InsumoResponse;
import com.deusto.coffeestack.dto.InsumoUpdateRequest;
import com.deusto.coffeestack.service.InsumoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InsumoController.class)
@AutoConfigureMockMvc(addFilters = false)
class InsumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InsumoService service;

    @MockBean
    private com.deusto.coffeestack.security.JwtAuthFilter jwtAuthFilter;

    @Test
    void listar_retornaPagina() throws Exception {
        InsumoResponse res = new InsumoResponse(1L, "Cafe", "KG", 10.0, true);
        Page<InsumoResponse> page = new PageImpl<>(List.of(res));
        when(service.listar(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Cafe"));
    }

    @Test
    void obtener_retornaInsumo() throws Exception {
        InsumoResponse res = new InsumoResponse(1L, "Cafe", "KG", 10.0, true);
        when(service.obtenerPorId(1L)).thenReturn(res);

        mockMvc.perform(get("/api/insumos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cafe"));
    }

    @Test
    void crear_retorna201() throws Exception {
        InsumoCreateRequest req = new InsumoCreateRequest();
        req.setNombre("Cafe");
        req.setUnidadMedida("KG");
        req.setStockMinimoAlerta(5.0);

        InsumoResponse res = new InsumoResponse(1L, "Cafe", "KG", 5.0, true);
        when(service.crear(any())).thenReturn(res);

        mockMvc.perform(post("/api/insumos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/insumos/1"));
    }

    @Test
    void actualizar_retornaInsumo() throws Exception {
        InsumoUpdateRequest req = new InsumoUpdateRequest();
        req.setNombre("Cafe");
        req.setUnidadMedida("KG");
        req.setStockMinimoAlerta(5.0);

        InsumoResponse res = new InsumoResponse(1L, "Cafe", "KG", 5.0, true);
        when(service.actualizar(eq(1L), any())).thenReturn(res);

        mockMvc.perform(put("/api/insumos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void desactivar_retornaNoContent() throws Exception {
        mockMvc.perform(delete("/api/insumos/1"))
                .andExpect(status().isNoContent());
        verify(service).desactivar(1L);
    }
}
