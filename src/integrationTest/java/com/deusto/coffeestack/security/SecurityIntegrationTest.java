package com.deusto.coffeestack.security;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.dto.AuthResponse;
import com.deusto.coffeestack.dto.LoginRequest;
import com.deusto.coffeestack.dto.RegisterRequest;
import com.deusto.coffeestack.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    void flujoCompletoAutenticacion_registroLoginAccesoProtected() throws Exception {
        // 1. Registro
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername("testuser");
        regReq.setPassword("password123");
        regReq.setRol(RolEnum.EMPLEADO);

        String regResString = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("EMPLEADO"))
                .andReturn().getResponse().getContentAsString();

        AuthResponse regRes = objectMapper.readValue(regResString, AuthResponse.class);
        String token = regRes.getToken();

        // 2. Acceso a ruta protegida con token de registro
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        // 3. Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("testuser");
        loginReq.setPassword("password123");

        String loginResString = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        AuthResponse loginRes = objectMapper.readValue(loginResString, AuthResponse.class);
        String loginToken = loginRes.getToken();

        // 4. Acceso con token de login
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void accesoSinToken_retornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConCredencialesIncorrectas_retornaUnauthorized() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername("testuser");
        regReq.setPassword("password123");
        regReq.setRol(RolEnum.EMPLEADO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("testuser");
        loginReq.setPassword("wrongpassword");

        // Generalmente Spring Security tira 401, o si se maneja con excepcion en el AuthenticationManager
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
}
