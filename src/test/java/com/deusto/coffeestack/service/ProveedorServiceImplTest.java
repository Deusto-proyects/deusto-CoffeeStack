package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Proveedor;
import com.deusto.coffeestack.dto.ProveedorCreateRequest;
import com.deusto.coffeestack.dto.ProveedorResponse;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock
    private ProveedorRepository repository;

    @InjectMocks
    private ProveedorServiceImpl service;

    private Proveedor sampleProveedor() {
        Proveedor p = new Proveedor();
        p.setId(1L);
        p.setNombre("Proveedor 1");
        p.setContacto("Contacto 1");
        p.setEmail("test@test.com");
        p.setTelefono("123456");
        p.setActivo(true);
        return p;
    }

    @Test
    void listar_devuelvePaginaDeProveedores() {
        Page<Proveedor> page = new PageImpl<>(List.of(sampleProveedor()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProveedorResponse> result = service.listar(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNombre()).isEqualTo("Proveedor 1");
    }

    @Test
    void obtenerPorId_devuelveProveedor() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleProveedor()));
        ProveedorResponse response = service.obtenerPorId(1L);
        assertThat(response.getNombre()).isEqualTo("Proveedor 1");
    }

    @Test
    void obtenerPorId_lanzaNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerPorId(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void crear_creaProveedor() {
        ProveedorCreateRequest req = new ProveedorCreateRequest();
        req.setNombre(" Nuevo ");
        req.setContacto("  ");
        req.setEmail("a@a.com");
        
        when(repository.existsByNombreIgnoreCase("Nuevo")).thenReturn(false);
        when(repository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        ProveedorResponse res = service.crear(req);

        assertThat(res.getNombre()).isEqualTo("Nuevo");
        assertThat(res.getContacto()).isNull();
        assertThat(res.getEmail()).isEqualTo("a@a.com");
    }

    @Test
    void crear_lanzaExceptionSiYaExiste() {
        ProveedorCreateRequest req = new ProveedorCreateRequest();
        req.setNombre("Proveedor 1");
        when(repository.existsByNombreIgnoreCase("Proveedor 1")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizar_actualizaProveedor() {
        Proveedor p = sampleProveedor();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        ProveedorCreateRequest req = new ProveedorCreateRequest();
        req.setNombre("Proveedor 1"); // Mismo nombre
        req.setContacto("Nuevo Contacto");

        ProveedorResponse res = service.actualizar(1L, req);

        assertThat(res.getContacto()).isEqualTo("Nuevo Contacto");
    }

    @Test
    void actualizar_cambiaNombreYVerificaExistencia() {
        Proveedor p = sampleProveedor();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.existsByNombreIgnoreCase("Otro")).thenReturn(true);

        ProveedorCreateRequest req = new ProveedorCreateRequest();
        req.setNombre("Otro");

        assertThatThrownBy(() -> service.actualizar(1L, req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cambiarEstado_cambiaEstado() {
        Proveedor p = sampleProveedor();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        
        service.cambiarEstado(1L, false);

        assertThat(p.isActivo()).isFalse();
        verify(repository).save(p);
    }

    @Test
    void eliminar_eliminaProveedor() {
        Proveedor p = sampleProveedor();
        when(repository.findById(1L)).thenReturn(Optional.of(p));

        service.eliminar(1L);

        verify(repository).delete(p);
    }

    @Test
    void eliminar_lanzaExceptionSiHayClaveForanea() {
        Proveedor p = sampleProveedor();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        doThrow(new DataIntegrityViolationException("")).when(repository).delete(p);

        assertThatThrownBy(() -> service.eliminar(1L)).isInstanceOf(IllegalArgumentException.class);
    }
}
