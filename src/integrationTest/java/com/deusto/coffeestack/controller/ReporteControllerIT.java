package com.deusto.coffeestack.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReporteControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void consumoPorInsumo_comoPropietarioConInsumoInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/reportes/consumo")
                        .param("insumoId", "999999")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void consumoPorInsumo_comoEmpleado_devuelve403() throws Exception {
        mockMvc.perform(get("/api/reportes/consumo")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    void consumoPorInsumo_sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/reportes/consumo")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-01-31"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void consumoPorInsumo_rangoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/reportes/consumo")
                        .param("insumoId", "1")
                        .param("desde", "2026-01-31")
                        .param("hasta", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }
}
