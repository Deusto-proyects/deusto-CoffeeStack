package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Item;
import com.deusto.coffeestack.dto.ItemCreateRequest;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    ItemRepository repository;

    @InjectMocks
    ItemServiceImpl service;

    @Test
    void create_shouldPersist() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setName("Test");
        req.setDescription("Desc");

        Item saved = new Item();
        saved.setId(1L);
        saved.setName("Test");
        saved.setDescription("Desc");

        when(repository.save(any(Item.class))).thenReturn(saved);

        var response = service.create(req);

        assertEquals(1L, response.getId());
        assertEquals("Test", response.getName());
        verify(repository, times(1)).save(any(Item.class));
    }

    @Test
    void getById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void update_shouldChangeFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Old");
        item.setDescription("Old");

        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("New");
        savedItem.setDescription("New");

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(Item.class))).thenReturn(savedItem);

        var req = new com.deusto.coffeestack.dto.ItemUpdateRequest();
        req.setName("New");
        req.setDescription("New");

        var response = service.update(1L, req);

        assertEquals("New", response.getName());
        verify(repository).findById(1L);
        verify(repository, times(1)).save(any(Item.class));
    }
}
