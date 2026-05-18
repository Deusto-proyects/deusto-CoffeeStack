package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.PreguntaChatbotRequest;
import com.deusto.coffeestack.dto.RespuestaChatbotResponse;
import com.deusto.coffeestack.exception.ChatbotUnavailableException;
import com.deusto.coffeestack.service.ChatbotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatbotController — tests unitarios")
class ChatbotControllerTest {

    @Mock ChatbotService chatbotService;

    @InjectMocks ChatbotController controller;

    @Test
    @DisplayName("preguntar: devuelve la respuesta del servicio con timestamp y latencia >= 0")
    void preguntar_pasaPreguntaAlServicio_yDevuelveDto() {
        when(chatbotService.responder("¿Qué reponer hoy?")).thenReturn("Repón el café.");

        RespuestaChatbotResponse response = controller.preguntar(
                new PreguntaChatbotRequest("¿Qué reponer hoy?"));

        assertThat(response.getRespuesta()).isEqualTo("Repón el café.");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getLatenciaMs()).isGreaterThanOrEqualTo(0L);
        verify(chatbotService).responder("¿Qué reponer hoy?");
    }

    @Test
    @DisplayName("preguntar: si el servicio lanza ChatbotUnavailableException, propaga")
    void preguntar_propaga_chatbotUnavailable() {
        when(chatbotService.responder("hola"))
                .thenThrow(new ChatbotUnavailableException("ollama caído"));

        assertThatThrownBy(() -> controller.preguntar(new PreguntaChatbotRequest("hola")))
                .isInstanceOf(ChatbotUnavailableException.class);
    }
}
