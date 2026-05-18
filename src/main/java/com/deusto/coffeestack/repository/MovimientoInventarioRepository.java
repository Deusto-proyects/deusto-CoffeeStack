package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ReporteMotivoResponse;
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

    /**
     * Returns the movements that reduce stock (VENTA, MERMA, ROTURA, AJUSTE_NEGATIVO)
     * for a given insumo within the [desde, hasta] window.
     *
     * <p>Used to compute average daily consumption without loading the full
     * movement history into memory.
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE " +
           "m.lote.insumo.id = :insumoId AND " +
           "m.tipoMovimiento IN (" +
           "  com.deusto.coffeestack.domain.TipoMovimiento.VENTA, " +
           "  com.deusto.coffeestack.domain.TipoMovimiento.MERMA, " +
           "  com.deusto.coffeestack.domain.TipoMovimiento.ROTURA, " +
           "  com.deusto.coffeestack.domain.TipoMovimiento.AJUSTE_NEGATIVO" +
           ") AND " +
           "m.fechaHora BETWEEN :desde AND :hasta")
    List<MovimientoInventario> findMovimientosSalidaByInsumoAndRango(
            @Param("insumoId") Long insumoId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * Agrupa movimientos por motivo y tipo dentro de un rango temporal,
     * agregando cantidad total y número de incidencias. Sin filtro de tipo.
     *
     * <p>Se ofrece como variante separada (vs. una única consulta con
     * {@code :tipo IS NULL}) porque PostgreSQL no puede inferir el tipo de
     * un parámetro JDBC cuando solo aparece comparado contra {@code NULL},
     * y devuelve "could not determine data type of parameter".
     */
    @Query("SELECT new com.deusto.coffeestack.dto.ReporteMotivoResponse(" +
           "  m.motivo, m.tipoMovimiento, COUNT(m), SUM(m.cantidad), " +
           "  MIN(m.fechaHora), MAX(m.fechaHora)) " +
           "FROM MovimientoInventario m WHERE " +
           "m.fechaHora BETWEEN :desde AND :hasta " +
           "GROUP BY m.motivo, m.tipoMovimiento " +
           "ORDER BY SUM(m.cantidad) DESC")
    List<ReporteMotivoResponse> agruparPorMotivo(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /** Igual que {@link #agruparPorMotivo(LocalDateTime, LocalDateTime)} pero filtrando por un tipo concreto. */
    @Query("SELECT new com.deusto.coffeestack.dto.ReporteMotivoResponse(" +
           "  m.motivo, m.tipoMovimiento, COUNT(m), SUM(m.cantidad), " +
           "  MIN(m.fechaHora), MAX(m.fechaHora)) " +
           "FROM MovimientoInventario m WHERE " +
           "m.tipoMovimiento = :tipo AND " +
           "m.fechaHora BETWEEN :desde AND :hasta " +
           "GROUP BY m.motivo, m.tipoMovimiento " +
           "ORDER BY SUM(m.cantidad) DESC")
    List<ReporteMotivoResponse> agruparPorMotivoYTipo(
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
