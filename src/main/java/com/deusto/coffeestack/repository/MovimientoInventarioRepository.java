package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Returns all movements for batches belonging to a given insumo,
     * sorted most-recent first.
     *
     * <p>Uses an explicit JPQL query to avoid Spring Data's derived-query
     * ambiguity between "Lote" + "insumoId" vs "LoteInsumo" + "Id".
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE m.lote.insumo.id = :insumoId ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findByInsumoIdOrderByFechaHoraDesc(@Param("insumoId") Long insumoId);
}
