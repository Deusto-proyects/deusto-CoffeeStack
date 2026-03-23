package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.ItemCreateRequest;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ROOT")
    void create_and_get() throws Exception {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setName("Item 1");
        req.setDescription("Desc");

        String body = objectMapper.writeValueAsString(req);

        String location = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/items/")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Item 1"));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void list_shouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_crearItem_devuelve403() throws Exception {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setName("Forbid");
        req.setDescription("x");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
