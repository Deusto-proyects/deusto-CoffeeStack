package com.deusto.coffeestack.config;

import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void loadUserByUsername_usuarioExisteYActivo_devuelveUserDetails() {
        Usuario u = new Usuario();
        u.setUsername("admin");
        u.setPasswordHash("hash123");
        u.setRol(RolEnum.ROOT);
        u.setActivo(true);
        when(repository.findByUsername("admin")).thenReturn(Optional.of(u));

        UserDetails result = service.loadUserByUsername("admin");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("hash123", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ROOT")));
    }

    @Test
    void loadUserByUsername_usuarioNoExiste_lanzaExcepcion() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_usuarioInactivo_lanzaExcepcion() {
        Usuario u = new Usuario();
        u.setUsername("empleado1");
        u.setActivo(false);
        when(repository.findByUsername("empleado1")).thenReturn(Optional.of(u));

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("empleado1"));
    }
}
