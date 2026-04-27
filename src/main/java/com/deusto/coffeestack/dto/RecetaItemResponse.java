package com.deusto.coffeestack.dto;

public record RecetaItemResponse(
        Long id,
        Long insumoId,
        String insumoNombre,
        String unidadMedida,
        double cantidad
) {}
