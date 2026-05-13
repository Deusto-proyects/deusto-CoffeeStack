package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.dto.EstimacionConsumoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EstimacionConsumoServiceImpl implements EstimacionConsumoService {

    private final InsumoRepository insumoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public EstimacionConsumoServiceImpl(InsumoRepository insumoRepository,
                                        MovimientoInventarioRepository movimientoInventarioRepository) {
        this.insumoRepository = insumoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EstimacionConsumoResponse calcular(Long insumoId, int ventanaDias, int horizonteDias) {
        validarVentana(ventanaDias);
        if (horizonteDias < 0) {
            throw new IllegalArgumentException("horizonteDias no puede ser negativo");
        }

        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + insumoId));

        List<MovimientoInventario> movimientos = movimientosEnVentana(insumoId, ventanaDias);

        double consumoTotal = sumaCantidades(movimientos);
        double consumoMedioDiario = consumoTotal / ventanaDias;
        int diasConActividad = contarDiasConActividad(movimientos);
        double consumoProyectado = consumoMedioDiario * horizonteDias;

        return new EstimacionConsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                ventanaDias,
                consumoTotal,
                consumoMedioDiario,
                diasConActividad,
                horizonteDias,
                consumoProyectado);
    }

    @Override
    @Transactional(readOnly = true)
    public double calcularConsumoMedioDiario(Long insumoId, int ventanaDias) {
        validarVentana(ventanaDias);
        List<MovimientoInventario> movimientos = movimientosEnVentana(insumoId, ventanaDias);
        return sumaCantidades(movimientos) / ventanaDias;
    }

    // ---- helpers ----

    private void validarVentana(int ventanaDias) {
        if (ventanaDias < 1) {
            throw new IllegalArgumentException("ventanaDias debe ser >= 1");
        }
    }

    private List<MovimientoInventario> movimientosEnVentana(Long insumoId, int ventanaDias) {
        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = hasta.minusDays(ventanaDias);
        return movimientoInventarioRepository.findMovimientosSalidaByInsumoAndRango(insumoId, desde, hasta);
    }

    private double sumaCantidades(List<MovimientoInventario> movimientos) {
        return movimientos.stream()
                .mapToDouble(MovimientoInventario::getCantidad)
                .sum();
    }

    private int contarDiasConActividad(List<MovimientoInventario> movimientos) {
        return (int) movimientos.stream()
                .map(m -> m.getFechaHora().toLocalDate())
                .distinct()
                .count();
    }
}
