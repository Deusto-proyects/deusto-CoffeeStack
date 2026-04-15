package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.RecetaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaItemRepository extends JpaRepository<RecetaItem, Long> {

    List<RecetaItem> findByItemId(Long itemId);

    void deleteByItemId(Long itemId);

    boolean existsByItemIdAndInsumoId(Long itemId, Long insumoId);
}
