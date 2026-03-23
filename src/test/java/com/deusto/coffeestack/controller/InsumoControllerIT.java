package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.InsumoCreateRequest;
import com.deusto.coffeestack.dto.InsumoUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for InsumoController.
 *
 * <ul>
 *   <li>PROPIETARIO/ROOT can create, edit and deactivate insumos.</li>
 *   <li>EMPLEADO can list and get insumos but cannot mutate them (403).</li>
 *   <li>Unauthenticated callers get 401.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InsumoControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ── helpers ───────────────────────────────────────────────────────────────

    private InsumoCreateRequest crearRequest(String nombre) {
        InsumoCreateRequest req = new InsumoCreateRequest();
        req.setNombre(nombre);
        req.setUnidadMedida("kg");
        req.setStockMinimoAlerta(5.0);
        return req;
    }

    private InsumoUpdateRequest actualizarRequest(String nombre) {
        InsumoUpdateRequest req = new InsumoUpdateRequest();
        req.setNombre(nombre);
        req.setUnidadMedida("litros");
        req.setStockMinimoAlerta(10.0);
        return req;
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void crear_comoPropietario_devuelve201() throws Exception {
        String body = objectMapper.writeValueAsString(crearRequest("Café Arábica"));

        mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/insumos/")))
                .andExpect(jsonPath("$.nombre").value("Café Arábica"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void crear_y_obtener_comoRoot() throws Exception {
        String body = objectMapper.writeValueAsString(crearRequest("Azúcar Moreno"));

        String location = mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Azúcar Moreno"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void listar_comoEmpleado_devuelve200() throws Exception {
        mockMvc.perform(get("/api/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void editar_comoRoot_actualizaCampos() throws Exception {
        // Crear primero
        String location = mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest("Leche Entera"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        assert location != null;
        String id = location.substring(location.lastIndexOf('/') + 1);

        // Editar
        mockMvc.perform(put("/api/insumos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarRequest("Leche Desnatada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Leche Desnatada"))
                .andExpect(jsonPath("$.unidadMedida").value("litros"))
                .andExpect(jsonPath("$.stockMinimoAlerta").value(10.0));
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void desactivar_comoPropietario_devuelve204_y_insumoQuedaInactivo() throws Exception {
        // Crear primero
        String location = mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest("Canela"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        assert location != null;
        String id = location.substring(location.lastIndexOf('/') + 1);

        // Desactivar
        mockMvc.perform(delete("/api/insumos/" + id))
                .andExpect(status().isNoContent());

        // Verificar que activo=false
        mockMvc.perform(get("/api/insumos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/insumos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_crearInsumo_devuelve403() throws Exception {
        mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest("Cacao"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_editarInsumo_devuelve403() throws Exception {
        mockMvc.perform(put("/api/insumos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarRequest("X"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_desactivarInsumo_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/insumos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void crear_sinNombre_devuelve400() throws Exception {
        InsumoCreateRequest req = new InsumoCreateRequest();
        req.setUnidadMedida("kg");
        req.setStockMinimoAlerta(5.0);

        mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void obtener_insumoInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/insumos/99999"))
                .andExpect(status().isNotFound());
    }
}
