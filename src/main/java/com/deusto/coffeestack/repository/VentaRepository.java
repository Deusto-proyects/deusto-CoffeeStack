package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    /** Devuelve todas las ventas ordenadas de más reciente a más antigua. */
    List<Venta> findAllByOrderByFechaHoraDesc();
}
