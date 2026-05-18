package com.deusto.coffeestack.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@ContextConfiguration(classes = OllamaInvokerRetryTest.Config.class)
@TestPropertySource(properties = "ai.chatbot.retry-max-attempts=3")
@DisplayName("OllamaInvoker — retry sobre fallos transitorios")
class OllamaInvokerRetryTest {

    @EnableRetry
    @TestConfiguration
    static class Config {
        @Bean
        OllamaInvoker ollamaInvoker(OllamaChatModel chatModel) {
            return new OllamaInvoker(chatModel);
        }
    }

    @MockBean
    OllamaChatModel chatModel;

    @Autowired
    OllamaInvoker invoker;

    @Test
    @DisplayName("reintenta hasta 3 veces ante ResourceAccessException y devuelve OK si el último intento triunfa")
    void reintentaYRecupera() {
        ChatResponse ok = new ChatResponse(List.of(new Generation("ok")));
        when(chatModel.call(any(Prompt.class)))
                .thenThrow(new ResourceAccessException("timeout 1"))
                .thenThrow(new ResourceAccessException("timeout 2"))
                .thenReturn(ok);

        ChatResponse resp = invoker.invocar(new Prompt("hola"));

        assertThat(resp).isSameAs(ok);
        verify(chatModel, times(3)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("tras agotar los reintentos, propaga la última ResourceAccessException")
    void agotaReintentosYPropaga() {
        when(chatModel.call(any(Prompt.class)))
                .thenThrow(new ResourceAccessException("timeout 1"))
                .thenThrow(new ResourceAccessException("timeout 2"))
                .thenThrow(new ResourceAccessException("timeout 3"));

        assertThatThrownBy(() -> invoker.invocar(new Prompt("hola")))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("timeout 3");

        verify(chatModel, times(3)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("no reintenta ante RuntimeException genérica (solo retryFor declaradas)")
    void noReintentaErroresNoRetryables() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> invoker.invocar(new Prompt("hola")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        verify(chatModel, times(1)).call(any(Prompt.class));
    }
}
