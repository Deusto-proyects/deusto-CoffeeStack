package com.deusto.coffeestack.service;

import com.deusto.coffeestack.exception.ChatbotUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("ChatbotServiceImpl — tests unitarios")
class ChatbotServiceImplTest {

    private static final String TEMPLATE = "ROL: asistente CoffeeStack\nSNAPSHOT:\n{snapshot}";

    private OllamaInvoker ollamaInvoker;
    private ContextoNegocioService contextoNegocioService;
    private ChatbotServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        ollamaInvoker = mock(OllamaInvoker.class);
        contextoNegocioService = mock(ContextoNegocioService.class);
        service = new ChatbotServiceImpl(ollamaInvoker, contextoNegocioService,
                new ByteArrayResource(TEMPLATE.getBytes()));
    }

    @Test
    @DisplayName("responder: pregunta vacía lanza IllegalArgumentException sin tocar Ollama")
    void responder_preguntaVacia_lanzaIllegalArgument() {
        assertThatThrownBy(() -> service.responder("   "))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(ollamaInvoker);
    }

    @Test
    @DisplayName("responder: incluye el snapshot en el SystemMessage y la pregunta como UserMessage")
    void responder_construyePromptConSnapshotYPregunta() {
        when(contextoNegocioService.generarSnapshot()).thenReturn("## Datos:\nLeche urgente");
        when(ollamaInvoker.invocar(any(Prompt.class))).thenReturn(respuestaOk("Repón leche."));

        String respuesta = service.responder("¿Qué reponer?");

        assertThat(respuesta).isEqualTo("Repón leche.");

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(ollamaInvoker).invocar(captor.capture());
        List<Message> mensajes = captor.getValue().getInstructions();

        assertThat(mensajes).hasSize(2);
        assertThat(mensajes.get(0).getMessageType()).isEqualTo(MessageType.SYSTEM);
        assertThat(mensajes.get(0).getContent()).contains("Leche urgente");
        assertThat(mensajes.get(1).getMessageType()).isEqualTo(MessageType.USER);
        assertThat(mensajes.get(1).getContent()).isEqualTo("¿Qué reponer?");
    }

    @Test
    @DisplayName("responder: trim a la respuesta del modelo")
    void responder_trimEnRespuesta() {
        when(contextoNegocioService.generarSnapshot()).thenReturn("");
        when(ollamaInvoker.invocar(any(Prompt.class))).thenReturn(respuestaOk("  Respuesta con espacios  \n"));

        String respuesta = service.responder("¿Algo?");

        assertThat(respuesta).isEqualTo("Respuesta con espacios");
    }

    @Test
    @DisplayName("responder: respuesta vacía del modelo → ChatbotUnavailableException")
    void responder_respuestaVacia_lanzaChatbotUnavailable() {
        when(contextoNegocioService.generarSnapshot()).thenReturn("");
        when(ollamaInvoker.invocar(any(Prompt.class))).thenReturn(respuestaOk("   "));

        assertThatThrownBy(() -> service.responder("¿Algo?"))
                .isInstanceOf(ChatbotUnavailableException.class);
    }

    @Test
    @DisplayName("responder: si el invocador lanza RuntimeException genérica, se mapea a ChatbotUnavailableException")
    void responder_falloModelo_mapeaAChatbotUnavailable() {
        when(contextoNegocioService.generarSnapshot()).thenReturn("");
        when(ollamaInvoker.invocar(any(Prompt.class))).thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> service.responder("¿Algo?"))
                .isInstanceOf(ChatbotUnavailableException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    @DisplayName("responder: ResourceAccessException (timeout/red) → mensaje específico de 'tardando demasiado'")
    void responder_timeoutOllama_mensajeEspecifico() {
        when(contextoNegocioService.generarSnapshot()).thenReturn("");
        when(ollamaInvoker.invocar(any(Prompt.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> service.responder("¿Algo?"))
                .isInstanceOf(ChatbotUnavailableException.class)
                .hasMessageContaining("tardando demasiado");
    }

    // ---- helpers ----

    private static ChatResponse respuestaOk(String texto) {
        return new ChatResponse(List.of(new Generation(texto)));
    }
}
