package com.deusto.coffeestack.mapper;

import com.deusto.coffeestack.domain.Proveedor;
import com.deusto.coffeestack.dto.ProveedorResponse;

public final class ProveedorMapper {

    private ProveedorMapper() {
    }

    public static ProveedorResponse toResponse(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId(),
                proveedor.getNombre(),
                proveedor.getContacto(),
                proveedor.getEmail(),
                proveedor.getTelefono(),
                proveedor.isActivo()
        );
    }
}
