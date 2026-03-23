package com.deusto.coffeestack.mapper;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.dto.InsumoResponse;

public final class InsumoMapper {
    private InsumoMapper() { }

    public static InsumoResponse toResponse(Insumo insumo) {
        return new InsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                insumo.getStockMinimoAlerta(),
                insumo.isActivo()
        );
    }
}
