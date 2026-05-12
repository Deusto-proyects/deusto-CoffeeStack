package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.config.UserDetailsServiceImpl;
import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.dto.LoginRequest;
import com.deusto.coffeestack.dto.RegisterRequest;
import com.deusto.coffeestack.repository.UsuarioRepository;
import com.deusto.coffeestack.security.JwtAuthFilter;
import com.deusto.coffeestack.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void login_retornaTokenYDatosDelUsuario() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(mock(org.springframework.security.core.userdetails.UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("fake-token");
        
        Usuario u = new Usuario();
        u.setUsername("admin");
        u.setRol(RolEnum.ROOT);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(u));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-token"))
                .andExpect(jsonPath("$.role").value("ROOT"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void register_creaUsuarioYRetornaToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("nuevo");
        req.setPassword("password");
        req.setRol(RolEnum.EMPLEADO);

        when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hash");
        when(userDetailsService.loadUserByUsername("nuevo")).thenReturn(mock(org.springframework.security.core.userdetails.UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("fake-token");

        Usuario u = new Usuario();
        u.setUsername("nuevo");
        u.setRol(RolEnum.EMPLEADO);
        // Simulamos que tras grabar y buscar, devolvemos este.
        // AuthController accede a request.getRol() o RolEnum.EMPLEADO, pero el Assert será de la response.

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake-token"))
                .andExpect(jsonPath("$.role").value("EMPLEADO"));
    }

    @Test
    void register_conflictoSiUsuarioExiste() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existente");
        req.setPassword("password");

        when(usuarioRepository.findByUsername("existente")).thenReturn(Optional.of(new Usuario()));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void me_retornaDatosDelUsuarioAutenticado() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("admin");

        Usuario u = new Usuario();
        u.setUsername("admin");
        u.setRol(RolEnum.ROOT);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/auth/me").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROOT"))
                .andExpect(jsonPath("$.username").value("admin"));
    }
}
