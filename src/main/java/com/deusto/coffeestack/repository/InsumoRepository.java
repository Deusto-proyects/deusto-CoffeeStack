package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
}
