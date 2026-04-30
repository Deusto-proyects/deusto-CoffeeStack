package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemPerfTest {

    private static final int TOTAL_REQUESTS = 50;
    private static final int THREADS = 10;
    private static final long RNF1_MEDIA_MAX_MS = 2000;

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper objectMapper;

    private String jwt;

    @BeforeAll
    void login() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        ResponseEntity<String> resp = rest.postForEntity(url("/api/auth/login"), req, String.class);
        JsonNode body = objectMapper.readTree(resp.getBody());
        jwt = body.get("token").asText();
    }

    @Test
    void listadoItems_50peticionesConcurrentes_mediaBajo2s() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Future<Long>> futures = new ArrayList<>();

        long t0Global = System.nanoTime();
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            futures.add(executor.submit(() -> {
                long t0 = System.nanoTime();
                ResponseEntity<String> r = rest.exchange(
                        url("/api/items"), HttpMethod.GET, entity, String.class);
                long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
                if (!r.getStatusCode().is2xxSuccessful()) {
                    throw new IllegalStateException("HTTP " + r.getStatusCode());
                }
                return elapsedMs;
            }));
        }

        List<Long> latencias = new ArrayList<>();
        for (Future<Long> f : futures) {
            latencias.add(f.get(30, TimeUnit.SECONDS));
        }
        long totalMs = (System.nanoTime() - t0Global) / 1_000_000;
        executor.shutdownNow();

        double media = imprimirInforme("GET /api/items", latencias, totalMs);

        assertTrue(media < RNF1_MEDIA_MAX_MS,
                "RNF1: la media debe ser < " + RNF1_MEDIA_MAX_MS + " ms, fue " + media);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private double imprimirInforme(String etiqueta, List<Long> latencias, long totalMs) {
        Collections.sort(latencias);
        int n = latencias.size();
        long min = latencias.get(0);
        long max = latencias.get(n - 1);
        long p95 = latencias.get(Math.min(n - 1, (int) Math.round(n * 0.95) - 1));
        long p99 = latencias.get(Math.min(n - 1, (int) Math.round(n * 0.99) - 1));
        double media = latencias.stream().mapToLong(Long::longValue).average().orElse(0);
        double throughput = n / (totalMs / 1000.0);
        System.out.println("==========================================");
        System.out.println("Performance: " + etiqueta);
        System.out.println("  N peticiones : " + n);
        System.out.println("  Tiempo total : " + totalMs + " ms");
        System.out.println("  Throughput   : " + String.format("%.2f", throughput) + " req/s");
        System.out.println("  Media        : " + String.format("%.2f", media) + " ms");
        System.out.println("  p95          : " + p95 + " ms");
        System.out.println("  p99          : " + p99 + " ms");
        System.out.println("  Min / Max    : " + min + " / " + max + " ms");
        System.out.println("==========================================");
        return media;
    }
}
