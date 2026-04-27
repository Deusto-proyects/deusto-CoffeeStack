package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.ProveedorCreateRequest;
import com.deusto.coffeestack.dto.ProveedorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProveedorService {
    Page<ProveedorResponse> listar(Pageable pageable);
    ProveedorResponse obtenerPorId(Long id);
    ProveedorResponse crear(ProveedorCreateRequest request);
    ProveedorResponse actualizar(Long id, ProveedorCreateRequest request);
    void cambiarEstado(Long id, boolean activo);
    void eliminar(Long id);
}
