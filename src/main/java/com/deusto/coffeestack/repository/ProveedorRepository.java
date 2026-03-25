package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
