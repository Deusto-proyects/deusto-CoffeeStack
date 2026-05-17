package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import com.deusto.coffeestack.repository.RecetaItemRepository;
import com.deusto.coffeestack.repository.VentaLineaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReposicionControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InsumoRepository insumoRepository;

    @Autowired
    LoteRepository loteRepository;

    @Autowired
    MovimientoInventarioRepository movimientoRepository;

    @Autowired
    VentaLineaRepository ventaLineaRepository;

    @Autowired
    RecetaItemRepository recetaItemRepository;

    private Long lecheId;

    @BeforeEach
    void setUp() {
        movimientoRepository.deleteAll();
        ventaLineaRepository.deleteAll();
        recetaItemRepository.deleteAll();
        loteRepository.deleteAll();
        insumoRepository.deleteAll();

        Insumo leche = new Insumo();
        leche.setNombre("Leche");
        leche.setUnidadMedida("litros");
        leche.setStockMinimoAlerta(0.0);
        leche.setLeadTimeDias(7);
        leche.setDiasCobertura(14);
        leche.setActivo(true);
        leche = insumoRepository.save(leche);
        lecheId = leche.getId();

        // 1 lote con 5 litros disponibles
        Lote lote = new Lote();
        lote.setInsumo(leche);
        lote.setNumeroLote("LL-001");
        lote.setCantidadInicial(60.0);
        lote.setCantidadActual(5.0);
        lote = loteRepository.save(lote);

        // 30 movimientos VENTA dentro de la ventana de 30 días sumando 60 unidades
        // -> consumo medio diario = 60/30 = 2 unidades/día
        // Uso i=0..29 (en vez de 1..30) para evitar que el movimiento más antiguo se quede
        // fuera de la ventana del servicio por los milisegundos que tarda en llamar a now().
        LocalDateTime ahora = LocalDateTime.now();
        for (int i = 0; i < 30; i++) {
            MovimientoInventario m = new MovimientoInventario();
            m.setLote(lote);
            m.setTipoMovimiento(TipoMovimiento.VENTA);
            m.setCantidad(2.0);
            m.setMotivo("Venta " + (i + 1));
            m.setUsuario("test");
            m.setFechaHora(ahora.minusDays(i));
            movimientoRepository.save(m);
        }
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void listar_devuelveSugerenciaCalculada() throws Exception {
        // consumo=2/día, leadTime=7, cobertura=14 -> necesidad = 2 * 21 = 42
        // stock=5 -> cantidadSugerida = 42 - 5 = 37
        // umbralUrgente = 14, stock=5 < 14 -> URGENTE
        mockMvc.perform(get("/api/insumos/sugerencias-reposicion").param("ventana", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].insumoNombre").value("Leche"))
                .andExpect(jsonPath("$[0].stockActual").value(closeTo(5.0, 0.0001)))
                .andExpect(jsonPath("$[0].consumoMedioDiario").value(closeTo(2.0, 0.0001)))
                .andExpect(jsonPath("$[0].cantidadSugerida").value(closeTo(37.0, 0.0001)))
                .andExpect(jsonPath("$[0].nivelUrgencia").value("URGENTE"));
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void detalle_devuelveSugerenciaDelInsumo() throws Exception {
        mockMvc.perform(get("/api/insumos/" + lecheId + "/sugerencia-reposicion").param("ventana", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insumoNombre").value("Leche"))
                .andExpect(jsonPath("$.cantidadSugerida").value(closeTo(37.0, 0.0001)))
                .andExpect(jsonPath("$.leadTimeDias").value(7))
                .andExpect(jsonPath("$.diasCobertura").value(14));
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void detalle_insumoInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/insumos/99999/sugerencia-reposicion"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void listar_empleadoNoAutorizado_devuelve403() throws Exception {
        mockMvc.perform(get("/api/insumos/sugerencias-reposicion"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/insumos/sugerencias-reposicion"))
                .andExpect(status().isUnauthorized());
    }
}
