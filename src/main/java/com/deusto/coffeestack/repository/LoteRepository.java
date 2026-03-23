package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    /** Returns all batches for a given insumo, ordered by expiry date ascending (nulls last). */
    @Query("SELECT l FROM Lote l WHERE l.insumo.id = :insumoId ORDER BY l.fechaVencimiento ASC NULLS LAST")
    List<Lote> findByInsumoId(@Param("insumoId") Long insumoId);

    /** Sums the current quantity across all batches for an insumo. Returns 0.0 if no batches exist. */
    @Query("SELECT COALESCE(SUM(l.cantidadActual), 0.0) FROM Lote l WHERE l.insumo.id = :insumoId")
    double sumCantidadActualByInsumoId(@Param("insumoId") Long insumoId);
}
