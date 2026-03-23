package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Item;
import com.deusto.coffeestack.dto.ItemCreateRequest;
import com.deusto.coffeestack.dto.ItemResponse;
import com.deusto.coffeestack.dto.ItemUpdateRequest;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.mapper.ItemMapper;
import com.deusto.coffeestack.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;

    public ItemServiceImpl(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(ItemMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item no encontrado: " + id));
        return ItemMapper.toResponse(item);
    }

    @Override
    @Transactional
    public ItemResponse create(ItemCreateRequest request) {
        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        Item saved = repository.save(item);
        return ItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ItemResponse update(Long id, ItemUpdateRequest request) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item no encontrado: " + id));
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        Item saved = repository.save(item);
        return ItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Item no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
