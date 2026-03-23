package com.deusto.coffeestack.config;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the default ROOT admin user on startup if it doesn't already exist.
 *
 * <p>This solves the chicken-and-egg problem: the first time the app starts
 * there are no users, so nobody can create users. This initializer inserts
 * the seed user {@code admin / admin123} using the application's own
 * {@link PasswordEncoder}, so the hash is always correct regardless of the
 * BCrypt rounds configured.
 *
 * <p>The SQL migration V3 no longer needs the INSERT — this handles it.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRol(RolEnum.ROOT);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            System.out.println("[DataInitializer] Usuario 'admin' creado con rol ROOT.");
        }
    }
}
