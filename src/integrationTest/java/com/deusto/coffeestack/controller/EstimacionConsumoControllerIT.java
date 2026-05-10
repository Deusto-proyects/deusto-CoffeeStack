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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstimacionConsumoControllerIT {

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

    private Long cafeId;

    @BeforeEach
    void setUp() {
        movimientoRepository.deleteAll();
        ventaLineaRepository.deleteAll();
        recetaItemRepository.deleteAll();
        loteRepository.deleteAll();
        insumoRepository.deleteAll();

        Insumo cafe = new Insumo();
        cafe.setNombre("Café");
        cafe.setUnidadMedida("kg");
        cafe.setStockMinimoAlerta(0.0);
        cafe = insumoRepository.save(cafe);
        cafeId = cafe.getId();

        Lote lote = new Lote();
        lote.setInsumo(cafe);
        lote.setNumeroLote("LC-001");
        lote.setCantidadInicial(50.0);
        lote.setCantidadActual(25.0);
        lote = loteRepository.save(lote);

        // 5 ventas en 5 días distintos dentro de los últimos 10 días, sumando 25
        LocalDateTime ahora = LocalDateTime.now();
        double[] cantidades = {5.0, 4.0, 6.0, 7.0, 3.0};
        int[] diasAtras = {1, 3, 5, 7, 9};
        for (int i = 0; i < cantidades.length; i++) {
            MovimientoInventario m = new MovimientoInventario();
            m.setLote(lote);
            m.setTipoMovimiento(TipoMovimiento.VENTA);
            m.setCantidad(cantidades[i]);
            m.setMotivo("Venta " + (i + 1));
            m.setUsuario("test");
            m.setFechaHora(ahora.minusDays(diasAtras[i]));
            movimientoRepository.save(m);
        }
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void estimar_devuelveMediaYProyeccionEsperadas() throws Exception {
        mockMvc.perform(get("/api/insumos/" + cafeId + "/estimacion-consumo")
                        .param("ventana", "30")
                        .param("horizonte", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insumoId").value(cafeId))
                .andExpect(jsonPath("$.insumoNombre").value("Café"))
                .andExpect(jsonPath("$.unidadMedida").value("kg"))
                .andExpect(jsonPath("$.ventanaDias").value(30))
                .andExpect(jsonPath("$.horizonteDias").value(7))
                .andExpect(jsonPath("$.consumoTotalVentana").value(closeTo(25.0, 0.0001)))
                .andExpect(jsonPath("$.consumoMedioDiario").value(closeTo(25.0 / 30.0, 0.0001)))
                .andExpect(jsonPath("$.consumoProyectado").value(closeTo(25.0 / 30.0 * 7, 0.0001)))
                .andExpect(jsonPath("$.diasConActividad").value(5));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void estimar_usaParametrosPorDefectoCuandoNoSePasan() throws Exception {
        mockMvc.perform(get("/api/insumos/" + cafeId + "/estimacion-consumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventanaDias").value(30))
                .andExpect(jsonPath("$.horizonteDias").value(7));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void estimar_insumoInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/insumos/99999/estimacion-consumo"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/insumos/" + 1 + "/estimacion-consumo"))
                .andExpect(status().isUnauthorized());
    }
}
