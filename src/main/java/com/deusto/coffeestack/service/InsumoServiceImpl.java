package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.dto.InsumoCreateRequest;
import com.deusto.coffeestack.dto.InsumoResponse;
import com.deusto.coffeestack.dto.InsumoUpdateRequest;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.mapper.InsumoMapper;
import com.deusto.coffeestack.repository.InsumoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository repository;

    public InsumoServiceImpl(InsumoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InsumoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(InsumoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InsumoResponse obtenerPorId(Long id) {
        Insumo insumo = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + id));
        return InsumoMapper.toResponse(insumo);
    }

    @Override
    @Transactional
    public InsumoResponse crear(InsumoCreateRequest request) {
        Insumo insumo = new Insumo();
        insumo.setNombre(request.getNombre());
        insumo.setUnidadMedida(request.getUnidadMedida());
        insumo.setStockMinimoAlerta(request.getStockMinimoAlerta());
        insumo.setActivo(true);
        return InsumoMapper.toResponse(repository.save(insumo));
    }

    @Override
    @Transactional
    public InsumoResponse actualizar(Long id, InsumoUpdateRequest request) {
        Insumo insumo = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + id));
        insumo.setNombre(request.getNombre());
        insumo.setUnidadMedida(request.getUnidadMedida());
        insumo.setStockMinimoAlerta(request.getStockMinimoAlerta());
        return InsumoMapper.toResponse(repository.save(insumo));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Insumo insumo = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Insumo no encontrado: " + id));
        insumo.setActivo(false);
        repository.save(insumo);
    }
}
