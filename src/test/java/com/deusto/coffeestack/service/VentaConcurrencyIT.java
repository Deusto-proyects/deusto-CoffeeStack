package com.deusto.coffeestack.service;

import com.deusto.coffeestack.domain.Insumo;
import com.deusto.coffeestack.domain.Item;
import com.deusto.coffeestack.domain.Lote;
import com.deusto.coffeestack.domain.RecetaItem;
import com.deusto.coffeestack.dto.VentaLineaRequest;
import com.deusto.coffeestack.dto.VentaRequest;
import com.deusto.coffeestack.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class VentaConcurrencyIT {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RecetaItemRepository recetaItemRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private VentaLineaRepository ventaLineaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    private Long itemId;
    private Long insumoId;

    @BeforeEach
    void setUp() {
        limpiarBaseDatos();

        // 1. Crear Insumo
        Insumo insumo = new Insumo();
        insumo.setNombre("Café en Grano Concurrencia");
        insumo.setUnidadMedida("kg");
        insumo.setStockMinimoAlerta(1.0);
        insumo.setActivo(true);
        insumo = insumoRepository.save(insumo);
        insumoId = insumo.getId();

        // 2. Crear Item
        Item item = new Item();
        item.setName("Espresso Concurrente");
        item.setDescription("Test");
        item = itemRepository.save(item);
        itemId = item.getId();

        // 3. Crear Receta (0.2 kg por espresso)
        RecetaItem receta = new RecetaItem();
        receta.setItem(item);
        receta.setInsumo(insumo);
        receta.setCantidad(0.2);
        recetaItemRepository.save(receta);
    }

    @AfterEach
    void tearDown() {
        limpiarBaseDatos();
    }

    private void limpiarBaseDatos() {
        movimientoRepository.deleteAll();
        ventaLineaRepository.deleteAll();
        ventaRepository.deleteAll();
        recetaItemRepository.deleteAll();
        loteRepository.deleteAll();
        insumoRepository.deleteAll();
    }

    @Test
    void registrarVenta_conMultiplesHilos_mantieneIntegridadStock() throws InterruptedException {
        // Configuramos 10 kg de stock total (repartidos en 2 lotes de 5kg)
        // Esto da exactamente para 50 espressos (50 * 0.2 = 10.0 kg)
        Insumo insumo = insumoRepository.findById(insumoId).orElseThrow();
        
        Lote lote1 = new Lote();
        lote1.setInsumo(insumo);
        lote1.setNumeroLote("L-CONC-01");
        lote1.setCantidadInicial(5.0);
        lote1.setCantidadActual(5.0);
        loteRepository.save(lote1);

        Lote lote2 = new Lote();
        lote2.setInsumo(insumo);
        lote2.setNumeroLote("L-CONC-02");
        lote2.setCantidadInicial(5.0);
        lote2.setCantidadActual(5.0);
        loteRepository.save(lote2);

        // Intentaremos vender 60 espressos concurrentemente usando 60 hilos.
        // Solo 50 deberían tener éxito, 10 deberían fallar.
        int totalRequests = 60;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    VentaLineaRequest linea = new VentaLineaRequest();
                    linea.setItemId(itemId);
                    linea.setCantidadUnidades(1);

                    VentaRequest request = new VentaRequest();
                    request.setLineas(List.of(linea));

                    ventaService.registrarVenta(request, "tester");
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Esperamos a que todos los hilos terminen
        latch.await();
        executor.shutdown();

        // Validaciones:
        // 1. Exactamente 50 ventas exitosas y 10 fallidas
        assertEquals(50, successCount.get(), "Deberían haberse completado exactamente 50 ventas exitosas");
        assertEquals(10, failCount.get(), "Deberían haber fallado exactamente 10 ventas por falta de stock");

        // 2. El stock final en ambos lotes debe ser exactamente 0.0
        double stockFinal = loteRepository.sumCantidadActualByInsumoId(insumoId);
        assertEquals(0.0, stockFinal, 0.001, "El stock final debe ser exactamente 0");

        // 3. Ningún lote puede tener stock negativo
        List<Lote> lotes = loteRepository.findByInsumoId(insumoId);
        for (Lote l : lotes) {
            assertTrue(l.getCantidadActual() >= 0, "Ningún lote debería tener cantidad negativa");
        }
    }
}
