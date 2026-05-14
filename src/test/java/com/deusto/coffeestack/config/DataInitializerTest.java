package com.deusto.coffeestack.config;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer — tests unitarios")
class DataInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("run: si admin no existe, lo crea")
    void run_adminNoExiste_creaAdmin() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("hashedPassword");

        dataInitializer.run(applicationArguments);

        verify(usuarioRepository).save(argThat(u -> 
            u.getUsername().equals("admin") &&
            u.getPasswordHash().equals("hashedPassword") &&
            u.getRol() == RolEnum.ROOT &&
            u.isActivo()
        ));
    }

    @Test
    @DisplayName("run: si admin ya existe, no hace nada")
    void run_adminYaExiste_noHaceNada() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(new Usuario()));

        dataInitializer.run(applicationArguments);

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
