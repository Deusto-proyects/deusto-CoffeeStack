package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.RecetaRequest;
import com.deusto.coffeestack.dto.RecetaResponse;

public interface RecetaService {

    RecetaResponse definirReceta(Long itemId, RecetaRequest request);

    RecetaResponse obtenerReceta(Long itemId);

    void eliminarReceta(Long itemId);
}
