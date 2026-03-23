package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.domain.Usuario;
import com.deusto.coffeestack.dto.UsuarioCreateRequest;
import com.deusto.coffeestack.dto.UsuarioResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario.setActivo(true);
        return UsuarioResponse.from(repository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Override
    public UsuarioResponse cambiarRol(Long id, RolEnum nuevoRol) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
        usuario.setRol(nuevoRol);
        return UsuarioResponse.from(repository.save(usuario));
    }

    @Override
    public void desactivar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
        usuario.setActivo(false);
        repository.save(usuario);
    }
}
