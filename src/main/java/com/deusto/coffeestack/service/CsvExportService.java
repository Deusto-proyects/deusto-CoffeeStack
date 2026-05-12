package com.deusto.coffeestack.service;

import com.deusto.coffeestack.dto.PuntoSerieDTO;
import com.deusto.coffeestack.dto.ReporteConsumoResponse;
import com.deusto.coffeestack.dto.ReporteVentasDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de exportación a formato CSV.
 *
 * <p>Incluye BOM UTF-8 (\uFEFF) al principio de cada fichero para que
 * Microsoft Excel en Windows abra los caracteres españoles (tildes, ñ)
 * correctamente sin necesidad de importación manual.
 */
@Service
public class CsvExportService {

    private static final char SEP = ',';
    private static final String CRLF = "\r\n";

    /** BOM UTF-8 para compatibilidad con Excel */
    public static final String BOM = "\uFEFF";

    /**
     * Genera el CSV del reporte de ventas por día y producto.
     *
     * <p>Columnas: Fecha, Producto, Unidades Vendidas
     */
    public String ventasToCsv(List<ReporteVentasDTO> filas) {
        StringBuilder sb = new StringBuilder(BOM);
        sb.append("Fecha").append(SEP)
          .append("Producto").append(SEP)
          .append("Unidades Vendidas").append(CRLF);

        for (ReporteVentasDTO fila : filas) {
            sb.append(fila.getFecha()).append(SEP)
              .append(escapeCsv(fila.getNombreProducto())).append(SEP)
              .append(fila.getCantidadTotal()).append(CRLF);
        }
        return sb.toString();
    }

    /**
     * Genera el CSV del reporte de consumo de insumos.
     *
     * <p>Incluye una sección de resumen y luego la serie temporal.
     * Columnas serie: Fecha, Cantidad, Coste (€)
     */
    public String consumoToCsv(ReporteConsumoResponse reporte) {
        StringBuilder sb = new StringBuilder(BOM);

        // Cabecera de metadatos
        sb.append("Insumo").append(SEP).append(escapeCsv(reporte.getInsumoNombre())).append(CRLF);
        sb.append("Unidad").append(SEP).append(escapeCsv(reporte.getUnidadMedida())).append(CRLF);
        sb.append("Desde").append(SEP).append(reporte.getDesde()).append(CRLF);
        sb.append("Hasta").append(SEP).append(reporte.getHasta()).append(CRLF);
        sb.append("Granularidad").append(SEP).append(reporte.getGranularidad()).append(CRLF);
        sb.append("Total Cantidad").append(SEP)
          .append(String.format("%.2f", reporte.getTotalCantidad())).append(CRLF);
        sb.append("Coste Total (EUR)").append(SEP)
          .append(reporte.getCosteTotal() != null ? reporte.getCosteTotal().toPlainString() : "0.00")
          .append(CRLF);
        sb.append(CRLF);

        // Serie temporal
        sb.append("Fecha").append(SEP)
          .append("Cantidad (").append(reporte.getUnidadMedida()).append(")").append(SEP)
          .append("Coste (EUR)").append(CRLF);

        List<PuntoSerieDTO> serie = reporte.getSerie();
        if (serie != null) {
            for (PuntoSerieDTO punto : serie) {
                sb.append(punto.getFecha()).append(SEP)
                  .append(String.format("%.4f", punto.getCantidad())).append(SEP)
                  .append(punto.getCoste() != null ? punto.getCoste().toPlainString() : "0.00")
                  .append(CRLF);
            }
        }
        return sb.toString();
    }

    /**
     * Escapa un valor para CSV: si contiene coma, comilla o salto de línea,
     * lo envuelve en comillas dobles y duplica las comillas internas.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
