package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.dto.InsumoCreateRequest;
import com.deusto.coffeestack.dto.InsumoResponse;
import com.deusto.coffeestack.dto.InsumoUpdateRequest;
import com.deusto.coffeestack.exception.NotFoundException;
import com.deusto.coffeestack.repository.InsumoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsumoServiceImplTest {

    @Mock
    private InsumoRepository repository;

    @InjectMocks
    private InsumoServiceImpl service;

    private Insumo sampleInsumo() {
        Insumo i = new Insumo();
        i.setId(1L);
        i.setNombre("Café Grano");
        i.setUnidadMedida("KG");
        i.setStockMinimoAlerta(10.0);
        i.setActivo(true);
        return i;
    }

    @Test
    void listar_devuelvePaginaDeInsumos() {
        Page<Insumo> page = new PageImpl<>(List.of(sampleInsumo()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<InsumoResponse> result = service.listar(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNombre()).isEqualTo("Café Grano");
    }

    @Test
    void obtenerPorId_devuelveInsumo() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleInsumo()));
        InsumoResponse res = service.obtenerPorId(1L);
        assertThat(res.getNombre()).isEqualTo("Café Grano");
    }

    @Test
    void obtenerPorId_lanzaNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerPorId(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void crear_creaInsumo() {
        InsumoCreateRequest req = new InsumoCreateRequest();
        req.setNombre("Leche");
        req.setUnidadMedida("L");
        req.setStockMinimoAlerta(5.0);

        when(repository.save(any(Insumo.class))).thenAnswer(inv -> inv.getArgument(0));

        InsumoResponse res = service.crear(req);

        assertThat(res.getNombre()).isEqualTo("Leche");
        assertThat(res.isActivo()).isTrue();
    }

    @Test
    void actualizar_actualizaInsumo() {
        Insumo i = sampleInsumo();
        when(repository.findById(1L)).thenReturn(Optional.of(i));
        when(repository.save(any(Insumo.class))).thenAnswer(inv -> inv.getArgument(0));

        InsumoUpdateRequest req = new InsumoUpdateRequest();
        req.setNombre("Café Molido");
        req.setUnidadMedida("KG");
        req.setStockMinimoAlerta(20.0);

        InsumoResponse res = service.actualizar(1L, req);

        assertThat(res.getNombre()).isEqualTo("Café Molido");
        assertThat(res.getStockMinimoAlerta()).isEqualTo(20.0);
    }

    @Test
    void desactivar_desactivaInsumo() {
        Insumo i = sampleInsumo();
        when(repository.findById(1L)).thenReturn(Optional.of(i));

        service.desactivar(1L);

        assertThat(i.isActivo()).isFalse();
        verify(repository).save(i);
    }
}
