package com.deusto.coffeestack.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenApiConfig — tests unitarios")
class OpenApiConfigTest {

    @Test
    @DisplayName("openAPI bean no es nulo y tiene título correcto")
    void openAPI_returnsValidOpenAPI() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.openAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CoffeeStack API");
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}
