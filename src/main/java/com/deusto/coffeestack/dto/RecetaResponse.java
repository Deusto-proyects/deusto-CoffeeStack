package com.deusto.coffeestack.dto;

import java.util.List;

public record RecetaResponse(
        Long itemId,
        String itemName,
        List<RecetaItemResponse> ingredientes
) {}
