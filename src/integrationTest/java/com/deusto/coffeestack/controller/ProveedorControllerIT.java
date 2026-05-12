package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.Proveedor;
import com.deusto.coffeestack.dto.ProveedorCreateRequest;
import com.deusto.coffeestack.repository.ProveedorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProveedorControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProveedorRepository proveedorRepository;

    @BeforeEach
    void setUp() {
        proveedorRepository.deleteAll();
    }

    private ProveedorCreateRequest crearRequest(String nombre) {
        ProveedorCreateRequest req = new ProveedorCreateRequest();
        req.setNombre(nombre);
        req.setContacto("Laura");
        req.setEmail("laura@proveedor.com");
        req.setTelefono("600123123");
        return req;
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_puedeRegistrarProveedor() throws Exception {
        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest("Cafe Norte"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/proveedores/")))
                .andExpect(jsonPath("$.nombre").value("Cafe Norte"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_puedeListarProveedores() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Distribuciones Sur");
        proveedor.setActivo(true);
        proveedorRepository.save(proveedor);

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void proveedorDuplicado_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(crearRequest("Beans Supply"));

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ya existe un proveedor con ese nombre"));
    }

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void propietario_puedeCambiarEstado() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Proveedor Cambiar Estado");
        proveedor.setActivo(true);
        proveedor = proveedorRepository.save(proveedor);

        mockMvc.perform(patch("/api/proveedores/" + proveedor.getId() + "/estado")
                        .param("activo", "false"))
                .andExpect(status().isNoContent());

        Proveedor actualizado = proveedorRepository.findById(proveedor.getId()).orElseThrow();
        assertThat(actualizado.isActivo()).isFalse();
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleado_noPuedeCambiarEstado_devuelve403() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Proveedor Cambiar Estado Empleado");
        proveedor.setActivo(true);
        proveedor = proveedorRepository.save(proveedor);

        mockMvc.perform(patch("/api/proveedores/" + proveedor.getId() + "/estado")
                        .param("activo", "false"))
                .andExpect(status().isForbidden());
    }
}
