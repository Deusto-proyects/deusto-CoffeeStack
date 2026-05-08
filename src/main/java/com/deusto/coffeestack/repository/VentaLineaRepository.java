package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.VentaLinea;
import com.deusto.coffeestack.dto.ReporteVentasDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VentaLineaRepository extends JpaRepository<VentaLinea, Long> {

    /** Devuelve todas las líneas pertenecientes a una venta concreta. */
    List<VentaLinea> findByVentaId(Long ventaId);

    /** Genera el reporte de ventas agregadas por día y por producto. */
    @Query("SELECT new com.deusto.coffeestack.dto.ReporteVentasDTO(cast(v.fechaHora as date), i.name, SUM(vl.cantidadUnidades)) " +
           "FROM VentaLinea vl " +
           "JOIN vl.venta v " +
           "JOIN vl.item i " +
           "GROUP BY cast(v.fechaHora as date), i.name " +
           "ORDER BY cast(v.fechaHora as date) DESC, i.name ASC")
    List<ReporteVentasDTO> getReporteVentasDiarias();
}
