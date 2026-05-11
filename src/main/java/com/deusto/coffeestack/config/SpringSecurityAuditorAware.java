package com.deusto.coffeestack.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Resuelve el usuario actual para los campos @CreatedBy / @LastModifiedBy
 * de las entidades auditadas con Spring Data JPA.
 *
 * <p>Lee el nombre del usuario autenticado del SecurityContext (poblado por JwtAuthFilter).
 * Si no hay autenticación (arranque, jobs internos, anónimo) devuelve "system".
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of("system");
        }
        return Optional.of(auth.getName());
    }
}
