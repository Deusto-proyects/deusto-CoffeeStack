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

    @Override
    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorCreateRequest request) {
        Proveedor proveedor = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado: " + id));

        if (!proveedor.getNombre().equalsIgnoreCase(request.getNombre().trim()) &&
            repository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese nombre");
        }

        proveedor.setNombre(request.getNombre().trim());
        proveedor.setContacto(normalize(request.getContacto()));
        proveedor.setEmail(normalize(request.getEmail()));
        proveedor.setTelefono(normalize(request.getTelefono()));
        
        return ProveedorMapper.toResponse(repository.save(proveedor));
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Proveedor proveedor = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado: " + id));
        proveedor.setActivo(activo);
        repository.save(proveedor);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado: " + id));
        try {
            repository.delete(proveedor);
            repository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("No se puede eliminar el proveedor porque tiene compras o lotes asociados. En su lugar, puedes desactivarlo.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
