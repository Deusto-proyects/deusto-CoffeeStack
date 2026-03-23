package com.deusto.coffeestack.config;

import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads a {@link Usuario} from the database and wraps it as a Spring Security
 * {@link UserDetails} with authority {@code ROLE_<ROL>}.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository repository;

    public UserDetailsServiceImpl(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + username);
        }

        return new User(
                usuario.getUsername(),
                usuario.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}
