package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Proveedor;
import com.deusto.coffeestack.dto.ProveedorCreateRequest;
import com.deusto.coffeestack.dto.ProveedorResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.mapper.ProveedorMapper;
import com.deusto.coffeestack.repository.ProveedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository repository;

    public ProveedorServiceImpl(ProveedorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(ProveedorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Long id) {
        Proveedor proveedor = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado: " + id));
        return ProveedorMapper.toResponse(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponse crear(ProveedorCreateRequest request) {
        if (repository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese nombre");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.getNombre().trim());
        proveedor.setContacto(normalize(request.getContacto()));
        proveedor.setEmail(normalize(request.getEmail()));
        proveedor.setTelefono(normalize(request.getTelefono()));
        proveedor.setActivo(true);
        return ProveedorMapper.toResponse(repository.save(proveedor));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
