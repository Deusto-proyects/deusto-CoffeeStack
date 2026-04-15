package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.InsumoCreateRequest;
import com.deusto.coffeestack.dto.ItemCreateRequest;
import com.deusto.coffeestack.dto.RecetaItemRequest;
import com.deusto.coffeestack.dto.RecetaRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecetaControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ── helpers ────────────────────────────────────────────────────────────────

    private Long crearItem(String nombre) throws Exception {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setName(nombre);
        req.setDescription("desc");

        String location = mockMvc.perform(post("/api/items")
                        .with(user("root").roles("ROOT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        assert location != null;
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    private Long crearInsumo(String nombre) throws Exception {
        InsumoCreateRequest req = new InsumoCreateRequest();
        req.setNombre(nombre);
        req.setUnidadMedida("kg");
        req.setStockMinimoAlerta(5.0);

        String location = mockMvc.perform(post("/api/insumos")
                        .with(user("root").roles("ROOT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        assert location != null;
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    private String recetaBody(Long insumoId, double cantidad) throws Exception {
        RecetaItemRequest ingrediente = new RecetaItemRequest();
        ingrediente.setInsumoId(insumoId);
        ingrediente.setCantidad(cantidad);

        RecetaRequest request = new RecetaRequest();
        request.setIngredientes(List.of(ingrediente));

        return objectMapper.writeValueAsString(request);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void definirReceta_comoPropietario_devuelve200() throws Exception {
        Long itemId = crearItem("Café Latte IT");
        Long insumoId = crearInsumo("Café en grano IT");

        mockMvc.perform(put("/api/items/" + itemId + "/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recetaBody(insumoId, 0.02)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.ingredientes", hasSize(1)))
                .andExpect(jsonPath("$.ingredientes[0].cantidad").value(0.02));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void definirReceta_comoRoot_devuelve200() throws Exception {
        Long itemId = crearItem("Cappuccino IT");
        Long insumoId = crearInsumo("Leche entera IT");

        mockMvc.perform(put("/api/items/" + itemId + "/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recetaBody(insumoId, 0.15)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientes[0].insumoId").value(insumoId))
                .andExpect(jsonPath("$.ingredientes[0].cantidad").value(0.15));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void obtenerReceta_comoEmpleado_devuelve200() throws Exception {
        Long itemId = crearItem("Espresso IT");
        Long insumoId = crearInsumo("Café molido IT");

        mockMvc.perform(put("/api/items/" + itemId + "/receta")
                        .with(user("root").roles("ROOT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recetaBody(insumoId, 0.007)));

        mockMvc.perform(get("/api/items/" + itemId + "/receta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientes", isA(java.util.List.class)));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void obtenerReceta_sinReceta_devuelveListaVacia() throws Exception {
        Long itemId = crearItem("Agua con gas IT");

        mockMvc.perform(get("/api/items/" + itemId + "/receta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientes", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void eliminarReceta_comoPropietario_devuelve204() throws Exception {
        Long itemId = crearItem("Té verde IT");
        Long insumoId = crearInsumo("Té matcha IT");

        mockMvc.perform(put("/api/items/" + itemId + "/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recetaBody(insumoId, 0.01)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/items/" + itemId + "/receta"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/items/" + itemId + "/receta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientes", hasSize(0)));
    }

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/items/1/receta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_definirReceta_devuelve403() throws Exception {
        mockMvc.perform(put("/api/items/1/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredientes\":[{\"insumoId\":1,\"cantidad\":0.1}]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_eliminarReceta_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/items/1/receta"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void definirReceta_sinIngredientes_devuelve400() throws Exception {
        Long itemId = crearItem("Zumo naranja IT");

        RecetaRequest request = new RecetaRequest();
        request.setIngredientes(List.of());

        mockMvc.perform(put("/api/items/" + itemId + "/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void definirReceta_itemNoExiste_devuelve404() throws Exception {
        Long insumoId = crearInsumo("Insumo fantasma IT");

        mockMvc.perform(put("/api/items/99999/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recetaBody(insumoId, 0.1)))
                .andExpect(status().isNotFound());
    }
}
