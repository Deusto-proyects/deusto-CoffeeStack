package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.dto.UsuarioResponse;
import com.deusto.coffeestack.dto.UsuarioUpdateRequest;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceImplTest {

    private UsuarioRepository repository;
    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UsuarioServiceImpl(repository, passwordEncoder);
    }

    private Usuario sampleUsuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("juan");
        u.setPasswordHash("hash-viejo");
        u.setRol(RolEnum.EMPLEADO);
        u.setActivo(false);
        return u;
    }

    // ── activar ──────────────────────────────────────────────────────────────

    @Test
    void activar_marcaActivoTrueYDevuelveRespuesta() {
        Usuario usuario = sampleUsuario();
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse result = service.activar(1L);

        assertThat(result.isActivo()).isTrue();
        assertThat(usuario.isActivo()).isTrue();
    }

    @Test
    void activar_esIdempotenteSiYaActivo() {
        Usuario usuario = sampleUsuario();
        usuario.setActivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse result = service.activar(1L);

        assertThat(result.isActivo()).isTrue();
    }

    @Test
    void activar_lanzaNotFoundCuandoIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activar(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── editar ──────────────────────────────────────────────────────────────

    @Test
    void editar_cambiaUsernameYRehasheaPasswordSiSeProporciona() {
        Usuario usuario = sampleUsuario();
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.findByUsername("juancho")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("nuevaPass")).thenReturn("hash-nuevo");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setUsername("juancho");
        req.setPassword("nuevaPass");

        UsuarioResponse result = service.editar(1L, req);

        assertThat(result.getUsername()).isEqualTo("juancho");
        assertThat(usuario.getPasswordHash()).isEqualTo("hash-nuevo");
        verify(passwordEncoder).encode("nuevaPass");
    }

    @Test
    void editar_sinPasswordMantieneElHashAnterior() {
        Usuario usuario = sampleUsuario();
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setUsername("juan"); // mismo username
        req.setPassword(null);

        service.editar(1L, req);

        assertThat(usuario.getPasswordHash()).isEqualTo("hash-viejo");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void editar_lanzaSiNuevoUsernameYaExiste() {
        Usuario usuario = sampleUsuario();
        Usuario otro = sampleUsuario();
        otro.setId(2L);
        otro.setUsername("ocupado");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.findByUsername("ocupado")).thenReturn(Optional.of(otro));

        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setUsername("ocupado");

        assertThatThrownBy(() -> service.editar(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ocupado");
    }

    @Test
    void editar_lanzaNotFoundCuandoIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setUsername("loquesea");

        assertThatThrownBy(() -> service.editar(99L, req))
                .isInstanceOf(NotFoundException.class);
    }
}
