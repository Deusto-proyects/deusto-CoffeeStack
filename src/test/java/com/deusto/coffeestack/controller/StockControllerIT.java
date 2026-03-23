package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StockControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InsumoRepository insumoRepository;

    @Autowired
    LoteRepository loteRepository;

    private Long cafeId;

    @BeforeEach
    void setUp() {
        loteRepository.deleteAll();
        insumoRepository.deleteAll();

        Insumo cafe = new Insumo();
        cafe.setNombre("Café");
        cafe.setUnidadMedida("kg");
        cafe.setStockMinimoAlerta(5.0);
        cafe = insumoRepository.save(cafe);
        cafeId = cafe.getId();

        Lote lote1 = new Lote();
        lote1.setInsumo(cafe);
        lote1.setNumeroLote("LC-001");
        lote1.setCantidadInicial(10.0);
        lote1.setCantidadActual(8.0);
        loteRepository.save(lote1);

        Lote lote2 = new Lote();
        lote2.setInsumo(cafe);
        lote2.setNumeroLote("LC-002");
        lote2.setCantidadInicial(5.0);
        lote2.setCantidadActual(1.0);  // low stock – but total 9.0 > 5.0, no risk
        loteRepository.save(lote2);

        // Second insumo with shortage risk
        Insumo leche = new Insumo();
        leche.setNombre("Leche");
        leche.setUnidadMedida("litros");
        leche.setStockMinimoAlerta(20.0);
        leche = insumoRepository.save(leche);

        Lote lotLeche = new Lote();
        lotLeche.setInsumo(leche);
        lotLeche.setNumeroLote("LL-001");
        lotLeche.setCantidadInicial(15.0);
        lotLeche.setCantidadActual(3.0); // 3 < 20 → risk
        loteRepository.save(lotLeche);
    }

    @Test
    void getAllStock_returnsListWithBothInsumos() throws Exception {
        mockMvc.perform(get("/api/stock/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].insumo.nombre", hasItems("Café", "Leche")));
    }

    @Test
    void getStockByInsumo_cafeHasNoRisk() throws Exception {
        mockMvc.perform(get("/api/stock/insumos/" + cafeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insumo.nombre").value("Café"))
                .andExpect(jsonPath("$.cantidadTotal").value(9.0))
                .andExpect(jsonPath("$.tieneRiesgoFaltante").value(false))
                .andExpect(jsonPath("$.lotes", hasSize(2)));
    }

    @Test
    void getStockByInsumo_lecheHasRisk() throws Exception {
        mockMvc.perform(get("/api/stock/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.insumo.nombre=='Leche')].tieneRiesgoFaltante",
                        hasItem(true)));
    }

    @Test
    void getStockByInsumo_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/stock/insumos/99999"))
                .andExpect(status().isNotFound());
    }
}
