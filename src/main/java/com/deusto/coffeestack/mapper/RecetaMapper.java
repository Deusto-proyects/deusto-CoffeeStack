package com.deusto.coffeestack.mapper;

import com.deusto.coffeestack.domain.Item;
import com.deusto.coffeestack.domain.RecetaItem;
import com.deusto.coffeestack.dto.RecetaItemResponse;
import com.deusto.coffeestack.dto.RecetaResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class RecetaMapper {

    private RecetaMapper() { }

    public static RecetaItemResponse toItemResponse(RecetaItem recetaItem) {
        return new RecetaItemResponse(
                recetaItem.getId(),
                recetaItem.getInsumo().getId(),
                recetaItem.getInsumo().getNombre(),
                recetaItem.getInsumo().getUnidadMedida(),
                recetaItem.getCantidad()
        );
    }

    public static RecetaResponse toRecetaResponse(Item item, List<RecetaItem> ingredientes) {
        return new RecetaResponse(
                item.getId(),
                item.getName(),
                ingredientes.stream()
                        .map(RecetaMapper::toItemResponse)
                        .collect(Collectors.toList())
        );
    }
}
