package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AjusteControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired InsumoRepository insumoRepository;
    @Autowired LoteRepository loteRepository;
    @Autowired MovimientoInventarioRepository movimientoRepository;

    private Long loteId;

    @BeforeEach
    void setUp() {
        movimientoRepository.deleteAll();
        loteRepository.deleteAll();
        insumoRepository.deleteAll();

        Insumo cafe = new Insumo();
        cafe.setNombre("Café IT");
        cafe.setUnidadMedida("kg");
        cafe.setStockMinimoAlerta(5.0);
        cafe = insumoRepository.save(cafe);

        Lote lote = new Lote();
        lote.setInsumo(cafe);
        lote.setNumeroLote("IT-001");
        lote.setCantidadInicial(20.0);
        lote.setCantidadActual(15.0);
        lote = loteRepository.save(lote);
        loteId = lote.getId();
    }

    // ── Authentication / Authorization ──────────────────────────────────────

    @Test
    void postAjuste_sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("MERMA", 1.0, "Sin autenticar")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void postAjuste_comoEmpleado_devuelve403() throws Exception {
        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("MERMA", 1.0, "Empleado no puede")))
                .andExpect(status().isForbidden());
    }

    // ── Positive happy paths ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void postMerma_valida_devuelve201YActualizaLote() throws Exception {
        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("MERMA", 3.0, "Producto caducado en depósito")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("MERMA"))
                .andExpect(jsonPath("$.cantidad").value(3.0))
                .andExpect(jsonPath("$.motivo").value("Producto caducado en depósito"));

        // Verify stock was actually decremented in DB
        Lote loteActualizado = loteRepository.findById(loteId).orElseThrow();
        assertEquals(12.0, loteActualizado.getCantidadActual(), 0.001);
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void postAjustePositivo_valido_devuelve201YSumaCantidad() throws Exception {
        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("AJUSTE_POSITIVO", 5.0, "Sobrante detectado en recuento")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("AJUSTE_POSITIVO"));

        Lote loteActualizado = loteRepository.findById(loteId).orElseThrow();
        assertEquals(20.0, loteActualizado.getCantidadActual(), 0.001);
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void postRotura_valida_comoRoot_devuelve201() throws Exception {
        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("ROTURA", 2.0, "Accidente en almacén zona A")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("ROTURA"));
    }

    // ── Validation errors ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void postMerma_cantidadSuperaStock_devuelve400() throws Exception {
        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("MERMA", 999.0, "Cantidad imposible")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void postAjuste_motivoVacio_devuelve400() throws Exception {
        String body = """
                {
                  "loteId": %d,
                  "tipoMovimiento": "MERMA",
                  "cantidad": 1.0,
                  "motivo": ""
                }
                """.formatted(loteId);

        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void postAjuste_loteInexistente_devuelve404() throws Exception {
        String body = """
                {
                  "loteId": 99999,
                  "tipoMovimiento": "MERMA",
                  "cantidad": 1.0,
                  "motivo": "Lote que no existe"
                }
                """;

        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ── GET endpoints ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void getAjustes_autenticado_devuelve200() throws Exception {
        mockMvc.perform(get("/api/ajustes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void getAjustesPorInsumo_insumoExistente_devuelve200() throws Exception {
        Long insumoId = loteRepository.findById(loteId).orElseThrow().getInsumo().getId();
        mockMvc.perform(get("/api/ajustes/insumo/" + insumoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void getAjustesPorInsumo_insumoNoExistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/ajustes/insumo/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void listarMovimientos_trasMerma_contieneMerma() throws Exception {
        // Register one merma first
        mockMvc.perform(post("/api/ajustes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("MERMA", 1.0, "Producto caducado antes del listado")))
                .andExpect(status().isCreated());

        // Then list
        mockMvc.perform(get("/api/ajustes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].tipoMovimiento", hasItem("MERMA")));
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private String buildBody(String tipo, double cantidad, String motivo) {
        return """
                {
                  "loteId": %d,
                  "tipoMovimiento": "%s",
                  "cantidad": %s,
                  "motivo": "%s"
                }
                """.formatted(loteId, tipo, cantidad, motivo);
    }
}
