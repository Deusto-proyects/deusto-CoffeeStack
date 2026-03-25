package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.AjusteRequest;
import com.deusto.coffeestack.dto.MovimientoResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import com.deusto.coffeestack.repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AjusteServiceImpl implements AjusteService {

    private final LoteRepository loteRepository;
    private final InsumoRepository insumoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public AjusteServiceImpl(LoteRepository loteRepository,
                             InsumoRepository insumoRepository,
                             MovimientoInventarioRepository movimientoRepository) {
        this.loteRepository = loteRepository;
        this.insumoRepository = insumoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    /**
     * Registers the movement and updates the lote's current quantity.
     *
     * <p>Business rules:
     * <ol>
     *   <li>The lote must exist.</li>
     *   <li>For MERMA / ROTURA / AJUSTE_NEGATIVO: quantity must not exceed the
     *       lote's current available stock.</li>
     *   <li>The lote's {@code cantidadActual} is decreased or increased accordingly.</li>
     *   <li>A {@link MovimientoInventario} audit record is persisted.</li>
     * </ol>
     */
    @Override
    @Transactional
    public MovimientoResponse registrarAjuste(AjusteRequest request, String usuarioLogin) {

        Lote lote = loteRepository.findById(request.getLoteId())
                .orElseThrow(() -> new NotFoundException(
                        "Lote no encontrado: " + request.getLoteId()));

        TipoMovimiento tipo = request.getTipoMovimiento();
        double cantidad = request.getCantidad();

        if (esMovimientoNegativo(tipo)) {
            if (cantidad > lote.getCantidadActual()) {
                throw new IllegalArgumentException(
                        String.format(
                                "La cantidad a descontar (%.2f) supera el stock actual del lote '%s' (%.2f).",
                                cantidad, lote.getNumeroLote(), lote.getCantidadActual()));
            }
            lote.setCantidadActual(lote.getCantidadActual() - cantidad);
        } else {
            // AJUSTE_POSITIVO
            lote.setCantidadActual(lote.getCantidadActual() + cantidad);
        }

        loteRepository.save(lote);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setLote(lote);
        mov.setTipoMovimiento(tipo);
        mov.setCantidad(cantidad);
        mov.setMotivo(request.getMotivo());
        mov.setUsuario(usuarioLogin);
        mov.setFechaHora(LocalDateTime.now());
        movimientoRepository.save(mov);

        return toResponse(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientos() {
        return movimientoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientosPorInsumo(Long insumoId) {
        // Validate insumo exists first
        insumoRepository.findById(insumoId)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + insumoId));

        return movimientoRepository.findByInsumoIdOrderByFechaHoraDesc(insumoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---- helpers ----

    private boolean esMovimientoNegativo(TipoMovimiento tipo) {
        return tipo == TipoMovimiento.MERMA
                || tipo == TipoMovimiento.ROTURA
                || tipo == TipoMovimiento.AJUSTE_NEGATIVO;
    }

    private MovimientoResponse toResponse(MovimientoInventario m) {
        return new MovimientoResponse(
                m.getId(),
                m.getLote().getId(),
                m.getLote().getNumeroLote(),
                m.getLote().getInsumo().getNombre(),
                m.getTipoMovimiento(),
                m.getCantidad(),
                m.getMotivo(),
                m.getUsuario(),
                m.getFechaHora()
        );
    }
}
