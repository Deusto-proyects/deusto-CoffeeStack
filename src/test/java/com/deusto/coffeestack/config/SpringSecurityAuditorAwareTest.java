package com.deusto.coffeestack.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SpringSecurityAuditorAwareTest {

    private final SpringSecurityAuditorAware auditor = new SpringSecurityAuditorAware();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devuelveSystemSiNoHayAutenticacion() {
        SecurityContextHolder.clearContext();

        Optional<String> result = auditor.getCurrentAuditor();

        assertThat(result).contains("system");
    }

    @Test
    void devuelveSystemParaUsuarioAnonimo() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null));

        Optional<String> result = auditor.getCurrentAuditor();

        assertThat(result).contains("system");
    }

    @Test
    void devuelveElNombreDelUsuarioAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "ignored",
                        java.util.List.of()));

        Optional<String> result = auditor.getCurrentAuditor();

        assertThat(result).contains("admin");
    }
}
