package com.deusto.coffeestack.repository;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.MovimientoInventario;
import com.deusto.coffeestack.domain.TipoMovimiento;
import com.deusto.coffeestack.dto.ReporteMotivoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MovimientoInventarioRepositoryIT {

    @Autowired private MovimientoInventarioRepository movimientoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private InsumoRepository insumoRepository;

    private LocalDateTime hoy;

    @BeforeEach
    void setUp() {
        hoy = LocalDateTime.of(2026, 5, 1, 12, 0);

        Insumo insumo = new Insumo();
        insumo.setNombre("Café");
        insumo.setUnidadMedida("kg");
        insumo = insumoRepository.save(insumo);

        Lote lote = new Lote();
        lote.setInsumo(insumo);
        lote.setNumeroLote("L-001");
        lote.setCantidadInicial(100.0);
        lote.setCantidadActual(50.0);
        lote = loteRepository.save(lote);

        // 1. MERMA - "Caducidad" - hoy - cant 5.0
        crearMov(lote, TipoMovimiento.MERMA, 5.0, "Caducidad", hoy);
        // 2. MERMA - "Caducidad" - hoy - cant 2.0 (Total caducidad: 7.0, 2 incidencias)
        crearMov(lote, TipoMovimiento.MERMA, 2.0, "Caducidad", hoy);
        
        // 3. ROTURA - "Caída" - hoy - cant 1.5
        crearMov(lote, TipoMovimiento.ROTURA, 1.5, "Caída", hoy);

        // 4. MERMA - "Caducidad" - fuera de rango (hace 1 año)
        crearMov(lote, TipoMovimiento.MERMA, 10.0, "Caducidad", hoy.minusYears(1));
    }

    private void crearMov(Lote lote, TipoMovimiento tipo, double cant, String motivo, LocalDateTime fecha) {
        MovimientoInventario m = new MovimientoInventario();
        m.setLote(lote);
        m.setTipoMovimiento(tipo);
        m.setCantidad(cant);
        m.setMotivo(motivo);
        m.setUsuario("admin");
        m.setFechaHora(fecha);
        movimientoRepository.save(m);
    }

    @Test
    void agruparPorMotivo_agrupaYAcumulaCorrectamente() {
        LocalDateTime desde = hoy.minusDays(1);
        LocalDateTime hasta = hoy.plusDays(1);

        List<ReporteMotivoResponse> resultados = movimientoRepository.agruparPorMotivo(desde, hasta);

        assertThat(resultados).hasSize(2);
        
        // Resultados ordenados por cantidadTotal DESC
        ReporteMotivoResponse caducidad = resultados.get(0);
        assertThat(caducidad.getMotivo()).isEqualTo("Caducidad");
        assertThat(caducidad.getTipoMovimiento()).isEqualTo(TipoMovimiento.MERMA);
        assertThat(caducidad.getNumIncidencias()).isEqualTo(2);
        assertThat(caducidad.getCantidadTotal()).isEqualTo(7.0);

        ReporteMotivoResponse rotura = resultados.get(1);
        assertThat(rotura.getMotivo()).isEqualTo("Caída");
        assertThat(rotura.getTipoMovimiento()).isEqualTo(TipoMovimiento.ROTURA);
        assertThat(rotura.getNumIncidencias()).isEqualTo(1);
        assertThat(rotura.getCantidadTotal()).isEqualTo(1.5);
    }

    @Test
    void agruparPorMotivoYTipo_filtraPorTipoEspecifico() {
        LocalDateTime desde = hoy.minusDays(1);
        LocalDateTime hasta = hoy.plusDays(1);

        List<ReporteMotivoResponse> resultados = movimientoRepository.agruparPorMotivoYTipo(TipoMovimiento.ROTURA, desde, hasta);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getMotivo()).isEqualTo("Caída");
        assertThat(resultados.get(0).getCantidadTotal()).isEqualTo(1.5);
    }

    @Test
    void agrupaciones_respetanElRangoTemporal() {
        // Rango antiguo que solo atrapa el movimiento de hace 1 año
        LocalDateTime desde = hoy.minusYears(2);
        LocalDateTime hasta = hoy.minusMonths(6);

        List<ReporteMotivoResponse> resultados = movimientoRepository.agruparPorMotivo(desde, hasta);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getCantidadTotal()).isEqualTo(10.0);
    }
}
