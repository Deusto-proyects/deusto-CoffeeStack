package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.ProveedorCreateRequest;
import com.deusto.coffeestack.dto.ProveedorResponse;
import com.deusto.coffeestack.service.ProveedorService;
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

@WebMvcTest(ProveedorController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProveedorService service;

    @MockBean
    private com.deusto.coffeestack.security.JwtAuthFilter jwtAuthFilter;

    @Test
    void listar_retornaPagina() throws Exception {
        ProveedorResponse res = new ProveedorResponse(1L, "Prov1", "Cont1", "email", "tel", true);
        Page<ProveedorResponse> page = new PageImpl<>(List.of(res));
        when(service.listar(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Prov1"));
    }

    @Test
    void obtener_retornaProveedor() throws Exception {
        ProveedorResponse res = new ProveedorResponse(1L, "Prov1", "Cont1", "email", "tel", true);
        when(service.obtenerPorId(1L)).thenReturn(res);

        mockMvc.perform(get("/api/proveedores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Prov1"));
    }

    @Test
    void crear_retorna201() throws Exception {
        ProveedorCreateRequest req = new ProveedorCreateRequest();
        req.setNombre("Prov1");

        ProveedorResponse res = new ProveedorResponse(1L, "Prov1", null, null, null, true);
        when(service.crear(any())).thenReturn(res);

        mockMvc.perform(post("/api/proveedores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/proveedores/1"));
    }

    @Test
    void actualizar_retornaOk() throws Exception {
        ProveedorCreateRequest req = new ProveedorCreateRequest();
        req.setNombre("Prov1");

        ProveedorResponse res = new ProveedorResponse(1L, "Prov1", null, null, null, true);
        when(service.actualizar(eq(1L), any())).thenReturn(res);

        mockMvc.perform(put("/api/proveedores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado_retornaNoContent() throws Exception {
        mockMvc.perform(patch("/api/proveedores/1/estado?activo=false"))
                .andExpect(status().isNoContent());
        verify(service).cambiarEstado(1L, false);
    }

    @Test
    void eliminar_retornaNoContent() throws Exception {
        mockMvc.perform(delete("/api/proveedores/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }
}
