package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.Proveedor;
import com.deusto.coffeestack.dto.LoteCreateRequest;
import com.deusto.coffeestack.dto.LoteResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.mapper.LoteMapper;
import com.deusto.coffeestack.repository.InsumoRepository;
import com.deusto.coffeestack.repository.LoteRepository;
import com.deusto.coffeestack.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoteServiceImpl implements LoteService {

    private final LoteRepository loteRepository;
    private final InsumoRepository insumoRepository;
    private final ProveedorRepository proveedorRepository;

    public LoteServiceImpl(LoteRepository loteRepository,
                           InsumoRepository insumoRepository,
                           ProveedorRepository proveedorRepository) {
        this.loteRepository = loteRepository;
        this.insumoRepository = insumoRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    @Transactional
    public LoteResponse recibirLote(LoteCreateRequest request) {
        // Validate insumo exists and is active
        Insumo insumo = insumoRepository.findById(request.getInsumoId())
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + request.getInsumoId()));
        if (!insumo.isActivo()) {
            throw new IllegalArgumentException("El insumo está desactivado y no puede recibir lotes: " + insumo.getNombre());
        }

        // Optional proveedor
        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new NotFoundException("Proveedor no encontrado: " + request.getProveedorId()));
            if (!proveedor.isActivo()) {
                throw new IllegalArgumentException("El proveedor está desactivado: " + proveedor.getNombre());
            }
        }

        Lote lote = new Lote();
        lote.setInsumo(insumo);
        lote.setProveedor(proveedor);
        lote.setNumeroLote(request.getNumeroLote().trim());
        lote.setCantidadInicial(request.getCantidad());
        lote.setCantidadActual(request.getCantidad());
        lote.setFechaVencimiento(request.getFechaVencimiento());

        return LoteMapper.toResponse(loteRepository.save(lote));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteResponse> listarPorInsumo(Long insumoId) {
        if (!insumoRepository.existsById(insumoId)) {
            throw new NotFoundException("Insumo no encontrado: " + insumoId);
        }
        return loteRepository.findByInsumoId(insumoId)
                .stream()
                .map(LoteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LoteResponse obtenerPorId(Long id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lote no encontrado: " + id));
        return LoteMapper.toResponse(lote);
    }
}
