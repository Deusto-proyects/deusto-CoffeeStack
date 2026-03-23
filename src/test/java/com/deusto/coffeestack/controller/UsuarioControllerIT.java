package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.dto.CambiarRolRequest;
import com.deusto.coffeestack.dto.UsuarioCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UsuarioController.
 *
 * <ul>
 *   <li>ROOT can create, list, change roles and deactivate users.</li>
 *   <li>Unauthenticated callers get 401.</li>
 *   <li>EMPLEADO callers get 403.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ── helpers ──────────────────────────────────────────────────────────────

    private UsuarioCreateRequest empleadoRequest(String suffix) {
        UsuarioCreateRequest req = new UsuarioCreateRequest();
        req.setUsername("empleado" + suffix);
        req.setPassword("pass12345");
        req.setRol(RolEnum.EMPLEADO);
        return req;
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ROOT")
    void crearUsuario_comoRoot_devuelve201() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empleadoRequest("_create"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("empleado_create"))
                .andExpect(jsonPath("$.rol").value("EMPLEADO"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void listarUsuarios_comoRoot_devuelve200() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void cambiarRol_comoRoot_devuelve200() throws Exception {
        // First create a user to change role on
        String body = objectMapper.writeValueAsString(empleadoRequest("_cambiar"));
        String location = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        // Extract id from location header
        assert location != null;
        String id = location.substring(location.lastIndexOf('/') + 1);

        CambiarRolRequest cambiarRolRequest = new CambiarRolRequest();
        cambiarRolRequest.setRol(RolEnum.PROPIETARIO);

        mockMvc.perform(patch("/api/usuarios/" + id + "/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambiarRolRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("PROPIETARIO"));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void desactivarUsuario_comoRoot_devuelve204() throws Exception {
        String body = objectMapper.writeValueAsString(empleadoRequest("_deact"));
        String location = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        assert location != null;
        String id = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(delete("/api/usuarios/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void crearUsuario_sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empleadoRequest("_unauth"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void crearUsuario_comoEmpleado_devuelve403() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empleadoRequest("_forbidden"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROPIETARIO")
    void listarUsuarios_comoPropietario_devuelve403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }
}
