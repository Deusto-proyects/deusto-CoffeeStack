package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    /** All movements ordered most-recent first. */
    List<MovimientoInventario> findAllByOrderByFechaHoraDesc();

    /**
     * Filterable audit query for the owner/root history view.
     *
     * <p>All parameters are optional (pass {@code null} to skip each filter):
     * <ul>
     *   <li>{@code insumoId} – restrict to batches of a specific insumo.</li>
     *   <li>{@code tipo}     – restrict to a specific movement type.</li>
     *   <li>{@code desde}    – lower bound on {@code fechaHora} (inclusive).</li>
     *   <li>{@code hasta}    – upper bound on {@code fechaHora} (inclusive).</li>
     * </ul>
     * Results are returned most-recent first.
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE " +
           "(:insumoId IS NULL OR m.lote.insumo.id = :insumoId) AND " +
           "(:tipo IS NULL OR m.tipoMovimiento = :tipo) AND " +
           "(:desde IS NULL OR m.fechaHora >= :desde) AND " +
           "(:hasta IS NULL OR m.fechaHora <= :hasta) " +
           "ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findByFilters(
            @Param("insumoId") Long insumoId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
