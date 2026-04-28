package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

/**
 * Simulación de rendimiento para CoffeeStack API.
 *
 * Escenarios:
 *   1. Carga ligera  — 5 usuarios simultáneos (éxito esperado)
 *   2. Carga media   — rampa de 20 usuarios en 30 s (éxito esperado)
 *   3. Carga alta    — 50 usuarios simultáneos (posibles fallos)
 *   4. Spike test    — pico de 100 usuarios de golpe (prueba de estrés)
 *
 * Requisito: la aplicación debe estar corriendo en http://localhost:8080
 * Arrancar con: ./gradlew bootRun
 * Ejecutar con: ./gradlew gatlingRun
 */
public class CoffeeStackSimulation extends Simulation {

    // --- Protocolo HTTP base ---
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/CoffeeStack");

    // --- Escenario: login + consulta de stock + ver insumos ---
    private final ScenarioBuilder consultaStock = scenario("Consulta de stock")
            .exec(
                    http("1. Login")
                            .post("/api/auth/login")
                            .body(StringBody("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                            .asJson()
                            .check(status().is(200))
                            .check(jsonPath("$.token").saveAs("jwt"))
            )
            .pause(Duration.ofMillis(500))
            .exec(
                    http("2. Listar insumos")
                            .get("/api/insumos")
                            .header("Authorization", "Bearer #{jwt}")
                            .check(status().is(200))
            )
            .pause(Duration.ofMillis(500))
            .exec(
                    http("3. Ver stock global")
                            .get("/api/stock/insumos")
                            .header("Authorization", "Bearer #{jwt}")
                            .check(status().is(200))
            )
            .pause(Duration.ofMillis(500))
            .exec(
                    http("4. Listar ventas")
                            .get("/api/ventas")
                            .header("Authorization", "Bearer #{jwt}")
                            .check(status().in(200, 403))
            );

    // --- Escenario: solo login (para prueba de estrés de autenticación) ---
    private final ScenarioBuilder soloLogin = scenario("Estrés en login")
            .exec(
                    http("Login")
                            .post("/api/auth/login")
                            .body(StringBody("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                            .asJson()
                            .check(status().is(200))
            );

    // --- Configuración de los 4 escenarios ---
    {
        setUp(
                // Escenario 1: Carga ligera — 5 usuarios a la vez (éxito)
                consultaStock.injectOpen(
                        atOnceUsers(5)
                ).protocols(httpProtocol),

                // Escenario 2: Carga media — rampa de 20 usuarios en 30 s (éxito)
                consultaStock.injectOpen(
                        rampUsers(20).during(Duration.ofSeconds(30))
                ).protocols(httpProtocol),

                // Escenario 3: Carga alta — 50 usuarios simultáneos (posibles fallos)
                consultaStock.injectOpen(
                        atOnceUsers(50)
                ).protocols(httpProtocol),

                // Escenario 4: Spike test — pico de 100 usuarios (estrés máximo)
                soloLogin.injectOpen(
                        atOnceUsers(100)
                ).protocols(httpProtocol)
        );
    }
}
