package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.VentaLinea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaLineaRepository extends JpaRepository<VentaLinea, Long> {

    /** Devuelve todas las líneas pertenecientes a una venta concreta. */
    List<VentaLinea> findByVentaId(Long ventaId);
}
