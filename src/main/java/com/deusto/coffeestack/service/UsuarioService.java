package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.RolEnum;
import com.deusto.coffeestack.dto.UsuarioCreateRequest;
import com.deusto.coffeestack.dto.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioCreateRequest request);

    List<UsuarioResponse> listar();

    UsuarioResponse cambiarRol(Long id, RolEnum nuevoRol);

    void desactivar(Long id);
}
