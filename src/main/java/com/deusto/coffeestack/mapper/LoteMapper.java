package com.deusto.coffeestack.mapper;

import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.dto.LoteResponse;

public final class LoteMapper {

    private LoteMapper() { }

    public static LoteResponse toResponse(Lote lote) {
        String proveedorNombre = lote.getProveedor() != null
                ? lote.getProveedor().getNombre()
                : null;
        return new LoteResponse(
                lote.getId(),
                lote.getNumeroLote(),
                lote.getCantidadInicial(),
                lote.getCantidadActual(),
                lote.getFechaVencimiento(),
                proveedorNombre
        );
    }
}
