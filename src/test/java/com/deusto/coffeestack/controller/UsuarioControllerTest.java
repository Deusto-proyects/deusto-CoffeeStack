package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.dto.CambiarRolRequest;
import com.deusto.coffeestack.dto.UsuarioCreateRequest;
import com.deusto.coffeestack.dto.UsuarioResponse;
import com.deusto.coffeestack.dto.UsuarioUpdateRequest;
import com.deusto.coffeestack.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController — tests unitarios")
class UsuarioControllerTest {

    @Mock
    private UsuarioService service;

    @InjectMocks
    private UsuarioController controller;

    private UsuarioResponse usuarioResponse;

    @BeforeEach
    void setUp() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("testUser");
        u.setRol(RolEnum.EMPLEADO);
        u.setActivo(true);
        usuarioResponse = UsuarioResponse.from(u);
    }

    @Test
    @DisplayName("crear: solicitud válida → 201 Created con Location header")
    void crear_solicitudValida_devuelve201() {
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        request.setUsername("testUser");
        request.setPassword("password");
        request.setRol(RolEnum.EMPLEADO);

        when(service.crear(any(UsuarioCreateRequest.class))).thenReturn(usuarioResponse);

        ResponseEntity<UsuarioResponse> response = controller.crear(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(usuarioResponse);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/usuarios/1");
        verify(service).crear(request);
    }

    @Test
    @DisplayName("listar: sin usuarios → lista vacía")
    void listar_sinUsuarios_devuelveListaVacia() {
        when(service.listar()).thenReturn(Collections.emptyList());

        List<UsuarioResponse> response = controller.listar();

        assertThat(response).isEmpty();
        verify(service).listar();
    }

    @Test
    @DisplayName("listar: con usuarios → devuelve lista")
    void listar_conUsuarios_devuelveLista() {
        when(service.listar()).thenReturn(List.of(usuarioResponse));

        List<UsuarioResponse> response = controller.listar();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(1L);
        verify(service).listar();
    }

    @Test
    @DisplayName("editar: solicitud válida → devuelve usuario actualizado")
    void editar_solicitudValida_devuelveUsuario() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setUsername("newUser");
        request.setPassword("newPass");

        when(service.editar(eq(1L), any(UsuarioUpdateRequest.class))).thenReturn(usuarioResponse);

        UsuarioResponse response = controller.editar(1L, request);

        assertThat(response).isEqualTo(usuarioResponse);
        verify(service).editar(1L, request);
    }

    @Test
    @DisplayName("cambiarRol: solicitud válida → devuelve usuario actualizado")
    void cambiarRol_solicitudValida_devuelveUsuario() {
        CambiarRolRequest request = new CambiarRolRequest();
        request.setRol(RolEnum.PROPIETARIO);

        when(service.cambiarRol(1L, RolEnum.PROPIETARIO)).thenReturn(usuarioResponse);

        UsuarioResponse response = controller.cambiarRol(1L, request);

        assertThat(response).isEqualTo(usuarioResponse);
        verify(service).cambiarRol(1L, RolEnum.PROPIETARIO);
    }

    @Test
    @DisplayName("activar: usuario desactivado → devuelve usuario activado")
    void activar_usuarioDesactivado_devuelveUsuarioActivado() {
        when(service.activar(1L)).thenReturn(usuarioResponse);

        UsuarioResponse response = controller.activar(1L);

        assertThat(response).isEqualTo(usuarioResponse);
        verify(service).activar(1L);
    }

    @Test
    @DisplayName("desactivar: usuario existente → 204 No Content")
    void desactivar_usuarioExistente_devuelve204() {
        doNothing().when(service).desactivar(1L);

        ResponseEntity<Void> response = controller.desactivar(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).desactivar(1L);
    }
}
