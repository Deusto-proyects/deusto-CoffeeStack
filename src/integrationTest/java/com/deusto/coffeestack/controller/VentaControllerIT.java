package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.Item;
import com.deusto.coffeestack.domain.Venta;
import com.deusto.coffeestack.domain.VentaLinea;
import com.deusto.coffeestack.repository.ItemRepository;
import com.deusto.coffeestack.repository.VentaLineaRepository;
import com.deusto.coffeestack.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class VentaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private VentaLineaRepository ventaLineaRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        ventaLineaRepository.deleteAll();
        ventaRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void obtenerReporte_sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/ventas/reporte"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void obtenerReporte_comoEmpleado_devuelve403() throws Exception {
        mockMvc.perform(get("/api/ventas/reporte"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void obtenerReporte_comoPropietario_devuelve200() throws Exception {
        // Preparar datos
        Item item = new Item();
        item.setName("Cafe Solo");
        item.setDescription("Delicioso cafe");
        item = itemRepository.save(item);

        Venta venta = new Venta();
        venta.setUsuario("empleado1");
        venta.setFechaHora(LocalDateTime.now());
        venta = ventaRepository.save(venta);

        VentaLinea linea = new VentaLinea();
        linea.setVenta(venta);
        linea.setItem(item);
        linea.setCantidadUnidades(5);
        ventaLineaRepository.save(linea);

        // Act & Assert
        mockMvc.perform(get("/api/ventas/reporte")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreProducto", is("Cafe Solo")))
                .andExpect(jsonPath("$[0].cantidadTotal", is(5)));
    }
}
