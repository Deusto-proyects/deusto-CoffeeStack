package com.deusto.coffeestack.config;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class OllamaClientConfig {

    @Bean
    @Primary
    public OllamaApi ollamaApi(
            @Value("${spring.ai.ollama.base-url}") String baseUrl,
            @Value("${ai.chatbot.connect-timeout-seconds:10}") long connectSeconds,
            @Value("${ai.chatbot.read-timeout-seconds:120}") long readSeconds) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(connectSeconds).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(readSeconds).toMillis());

        RestClient.Builder builder = RestClient.builder().requestFactory(factory);
        return new OllamaApi(baseUrl, builder);
    }
}
