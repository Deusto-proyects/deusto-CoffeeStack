package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.*;
import com.deusto.coffeestack.dto.*;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de ventas.
 *
 * <p>Lógica de descuento de inventario:
 * <ol>
 *   <li>Para cada línea de venta se obtiene la receta del item.</li>
 *   <li>Por cada ingrediente se calcula la cantidad total necesaria
 *       (cantidadUnidades × cantidad_receta).</li>
 *   <li>Los lotes del insumo se consumen en orden FIFO (más próximo
 *       a vencer primero). Si el stock total es insuficiente se lanza
 *       {@link IllegalStateException} y la transacción se revierte.</li>
 *   <li>Por cada lote afectado se genera un {@link MovimientoInventario}
 *       de tipo {@link TipoMovimiento#VENTA}.</li>
 *   <li>Se persiste la {@link Venta} con sus {@link VentaLinea}.</li>
 * </ol>
 */
@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaLineaRepository ventaLineaRepository;
    private final ItemRepository itemRepository;
    private final RecetaItemRepository recetaItemRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            VentaLineaRepository ventaLineaRepository,
                            ItemRepository itemRepository,
                            RecetaItemRepository recetaItemRepository,
                            LoteRepository loteRepository,
                            MovimientoInventarioRepository movimientoRepository) {
        this.ventaRepository = ventaRepository;
        this.ventaLineaRepository = ventaLineaRepository;
        this.itemRepository = itemRepository;
        this.recetaItemRepository = recetaItemRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional
    public VentaResponse registrarVenta(VentaRequest request, String usuarioLogin) {

        // --- Fase 1: validar items y calcular necesidades de insumos ---
        // Mapa: insumoId → cantidad total necesaria
        java.util.Map<Long, Double> necesidades = new java.util.LinkedHashMap<>();
        // Mapa: insumoId → nombre (para mensajes de error)
        java.util.Map<Long, String> nombreInsumo = new java.util.HashMap<>();

        List<Item> items = new ArrayList<>();

        for (VentaLineaRequest linea : request.getLineas()) {
            Item item = itemRepository.findById(linea.getItemId())
                    .orElseThrow(() -> new NotFoundException(
                            "Producto no encontrado: " + linea.getItemId()));

            List<RecetaItem> ingredientes = recetaItemRepository.findByItemId(item.getId());
            if (ingredientes.isEmpty()) {
                throw new IllegalArgumentException(
                        "El producto '" + item.getName() + "' no tiene receta definida. " +
                        "Define la receta antes de registrar una venta.");
            }

            for (RecetaItem ri : ingredientes) {
                double cantidadNecesaria = ri.getCantidad() * linea.getCantidadUnidades();
                necesidades.merge(ri.getInsumo().getId(), cantidadNecesaria, Double::sum);
                nombreInsumo.put(ri.getInsumo().getId(), ri.getInsumo().getNombre());
            }
            items.add(item);
        }

        // --- Fase 2: verificar stock suficiente antes de descontar ---
        for (java.util.Map.Entry<Long, Double> entry : necesidades.entrySet()) {
            double stockDisponible = loteRepository.sumCantidadActualByInsumoId(entry.getKey());
            if (stockDisponible < entry.getValue()) {
                throw new IllegalStateException(String.format(
                        "Stock insuficiente para el insumo '%s': necesario=%.2f, disponible=%.2f.",
                        nombreInsumo.get(entry.getKey()), entry.getValue(), stockDisponible));
            }
        }

        // --- Fase 3: descontar lotes FIFO y generar movimientos ---
        for (java.util.Map.Entry<Long, Double> entry : necesidades.entrySet()) {
            Long insumoId = entry.getKey();
            double restante = entry.getValue();

            // findByInsumoId ya ordena por fechaVencimiento ASC (FIFO)
            List<Lote> lotes = loteRepository.findByInsumoId(insumoId);

            for (Lote lote : lotes) {
                if (restante <= 0) break;

                double descuento = Math.min(lote.getCantidadActual(), restante);
                if (descuento <= 0) continue;

                lote.setCantidadActual(lote.getCantidadActual() - descuento);
                loteRepository.save(lote);

                MovimientoInventario mov = new MovimientoInventario();
                mov.setLote(lote);
                mov.setTipoMovimiento(TipoMovimiento.VENTA);
                mov.setCantidad(descuento);
                mov.setMotivo("Descuento por venta registrada por " + usuarioLogin);
                mov.setUsuario(usuarioLogin);
                mov.setFechaHora(LocalDateTime.now());
                movimientoRepository.save(mov);

                restante -= descuento;
            }
        }

        // --- Fase 4: persistir la venta y sus líneas ---
        Venta venta = new Venta();
        venta.setUsuario(usuarioLogin);
        venta.setFechaHora(LocalDateTime.now());
        venta = ventaRepository.save(venta);

        List<VentaLineaResponse> lineaResponses = new ArrayList<>();
        List<VentaLineaRequest> lineasRequest = request.getLineas();

        for (int i = 0; i < lineasRequest.size(); i++) {
            VentaLineaRequest req = lineasRequest.get(i);
            Item item = items.get(i);

            VentaLinea linea = new VentaLinea();
            linea.setVenta(venta);
            linea.setItem(item);
            linea.setCantidadUnidades(req.getCantidadUnidades());
            ventaLineaRepository.save(linea);

            lineaResponses.add(new VentaLineaResponse(item.getId(), item.getName(), req.getCantidadUnidades()));
        }

        return new VentaResponse(venta.getId(), venta.getUsuario(), venta.getFechaHora(), lineaResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> listarVentas() {
        return ventaRepository.findAllByOrderByFechaHoraDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtenerVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada: " + id));
        return toResponse(venta);
    }

    // ---- helpers ----

    private VentaResponse toResponse(Venta venta) {
        List<VentaLineaResponse> lineas = ventaLineaRepository.findByVentaId(venta.getId())
                .stream()
                .map(l -> new VentaLineaResponse(
                        l.getItem().getId(),
                        l.getItem().getName(),
                        l.getCantidadUnidades()))
                .toList();
        return new VentaResponse(venta.getId(), venta.getUsuario(), venta.getFechaHora(), lineas);
    }
}
