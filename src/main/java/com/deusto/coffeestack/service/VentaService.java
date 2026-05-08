package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.ReporteVentasDTO;
import com.deusto.coffeestack.dto.VentaRequest;
import com.deusto.coffeestack.dto.VentaResponse;

import java.util.List;

/**
 * Contrato de servicio para el registro y consulta de ventas.
 */
public interface VentaService {

    /**
     * Registra una nueva venta descontando el inventario de cada insumo
     * implicado mediante la receta de cada producto, en orden FIFO de lotes.
     *
     * @param request      datos de la venta (lista de líneas con item + unidades)
     * @param usuarioLogin login del empleado autenticado
     * @return la venta registrada con sus líneas de detalle
     * @throws IllegalStateException    si el stock de algún insumo es insuficiente
     * @throws IllegalArgumentException si algún item no tiene receta definida
     */
    VentaResponse registrarVenta(VentaRequest request, String usuarioLogin);

    /** Devuelve todas las ventas, de más reciente a más antigua. */
    List<VentaResponse> listarVentas();

    /** Devuelve el detalle de una venta por su id. */
    VentaResponse obtenerVenta(Long id);

    /** Genera un reporte agregado de ventas por día y producto. */
    List<ReporteVentasDTO> obtenerReporteVentas();
}
