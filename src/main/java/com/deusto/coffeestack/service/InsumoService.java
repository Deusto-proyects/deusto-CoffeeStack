package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.InsumoCreateRequest;
import com.deusto.coffeestack.dto.InsumoResponse;
import com.deusto.coffeestack.dto.InsumoUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InsumoService {
    Page<InsumoResponse> listar(Pageable pageable);
    InsumoResponse obtenerPorId(Long id);
    InsumoResponse crear(InsumoCreateRequest request);
    InsumoResponse actualizar(Long id, InsumoUpdateRequest request);
    void desactivar(Long id);
}
