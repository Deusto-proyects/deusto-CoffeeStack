package com.deusto.coffeestack.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class RecetaRequest {

    @NotEmpty(message = "La receta debe contener al menos un ingrediente")
    @Valid
    private List<RecetaItemRequest> ingredientes;

    public List<RecetaItemRequest> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<RecetaItemRequest> ingredientes) { this.ingredientes = ingredientes; }
}
