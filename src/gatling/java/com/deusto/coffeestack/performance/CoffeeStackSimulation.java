package com.deusto.coffeestack.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Simulación de rendimiento con Gatling (Java DSL).
 * Simula a múltiples empleados autenticándose y registrando ventas
 * concurrentemente para evaluar el rendimiento y los bloqueos de inventario.
 */
public class CoffeeStackSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // Autenticación de un empleado (asumimos que existe un "admin" u otro usuario pre-cargado)
    ChainBuilder login = exec(http("Login")
            .post("/api/auth/login")
            .body(StringBody("{\"username\":\"admin\",\"password\":\"admin\"}"))
            .check(status().is(200))
            .check(jsonPath("$.token").saveAs("authToken"))
    ).exitHereIfFailed();

    // Registro de una venta (simulando venta del Item ID 1)
    ChainBuilder registrarVenta = exec(http("Registrar Venta")
            .post("/api/ventas")
            .header("Authorization", "Bearer #{authToken}")
            .body(StringBody("{ \"lineas\": [ { \"itemId\": 1, \"cantidadUnidades\": 1 } ] }"))
            // Aceptamos 201 (Created) o 400/409 si falla por falta de stock (es normal en pruebas de carga)
            .check(status().in(201, 400, 409))
    );

    // Escenario principal
    ScenarioBuilder scn = scenario("Carga Concurrente de Ventas")
            .exec(login)
            .pause(1)
            // Cada empleado registra 5 ventas con pausas pequeñas
            .repeat(5).on(
                    exec(registrarVenta)
                    .pause(Duration.ofMillis(300))
            );

    {
        // Simulamos 50 usuarios ingresando en un periodo de 10 segundos
        setUp(
                scn.injectOpen(rampUsers(50).during(Duration.ofSeconds(10)))
        ).protocols(httpProtocol);
    }
}
