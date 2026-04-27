package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Item;
import com.deusto.coffeestack.domain.RecetaItem;
import com.deusto.coffeestack.dto.RecetaItemRequest;
import com.deusto.coffeestack.dto.RecetaRequest;
import com.deusto.coffeestack.dto.RecetaResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.mapper.RecetaMapper;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.ItemRepository;
import com.deusto.coffeestack.repository.RecetaItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecetaServiceImpl implements RecetaService {

    private final RecetaItemRepository recetaItemRepository;
    private final ItemRepository itemRepository;
    private final InsumoRepository insumoRepository;

    public RecetaServiceImpl(RecetaItemRepository recetaItemRepository,
                              ItemRepository itemRepository,
                              InsumoRepository insumoRepository) {
        this.recetaItemRepository = recetaItemRepository;
        this.itemRepository = itemRepository;
        this.insumoRepository = insumoRepository;
    }

    @Override
    @Transactional
    public RecetaResponse definirReceta(Long itemId, RecetaRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item no encontrado: " + itemId));

        Set<Long> seen = new LinkedHashSet<>();
        List<Insumo> insumos = new ArrayList<>();
        for (RecetaItemRequest ingrediente : request.getIngredientes()) {
            if (!seen.add(ingrediente.getInsumoId())) {
                throw new IllegalArgumentException(
                        "El insumo " + ingrediente.getInsumoId() + " aparece más de una vez en la receta");
            }
            if (ingrediente.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            Insumo insumo = insumoRepository.findById(ingrediente.getInsumoId())
                    .orElseThrow(() -> new NotFoundException(
                            "Insumo no encontrado: " + ingrediente.getInsumoId()));
            if (!insumo.isActivo()) {
                throw new IllegalArgumentException(
                        "El insumo está desactivado: " + insumo.getNombre());
            }
            insumos.add(insumo);
        }

        recetaItemRepository.deleteByItemId(itemId);

        List<RecetaItemRequest> ingredientesList = request.getIngredientes();
        List<RecetaItem> savedItems = new ArrayList<>();
        for (int i = 0; i < ingredientesList.size(); i++) {
            RecetaItem recetaItem = new RecetaItem();
            recetaItem.setItem(item);
            recetaItem.setInsumo(insumos.get(i));
            recetaItem.setCantidad(ingredientesList.get(i).getCantidad());
            savedItems.add(recetaItemRepository.save(recetaItem));
        }

        return RecetaMapper.toRecetaResponse(item, savedItems);
    }

    @Override
    @Transactional(readOnly = true)
    public RecetaResponse obtenerReceta(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item no encontrado: " + itemId));
        List<RecetaItem> ingredientes = recetaItemRepository.findByItemId(itemId);
        return RecetaMapper.toRecetaResponse(item, ingredientes);
    }

    @Override
    @Transactional
    public void eliminarReceta(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Item no encontrado: " + itemId);
        }
        recetaItemRepository.deleteByItemId(itemId);
    }
}
