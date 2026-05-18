package com.deusto.coffeestack.service;

import com.deusto.coffeestack.exception.ChatbotUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    /** Cuántos caracteres del prompt se logean a nivel DEBUG. */
    private static final int LOG_PROMPT_CHARS = 500;

    private final OllamaInvoker ollamaInvoker;
    private final ContextoNegocioService contextoNegocioService;
    private final String systemPromptTemplate;

    public ChatbotServiceImpl(OllamaInvoker ollamaInvoker,
                              ContextoNegocioService contextoNegocioService,
                              @Value("classpath:prompts/system-chatbot.st") Resource systemPromptResource) throws IOException {
        this.ollamaInvoker = ollamaInvoker;
        this.contextoNegocioService = contextoNegocioService;
        this.systemPromptTemplate = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public String responder(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            throw new IllegalArgumentException("La pregunta no puede estar vacía");
        }

        String snapshot = contextoNegocioService.generarSnapshot();
        String systemText = new PromptTemplate(systemPromptTemplate, Map.of("snapshot", snapshot))
                .render();

        Prompt prompt = new Prompt(List.of(new SystemMessage(systemText), new UserMessage(pregunta)));

        long t0 = System.nanoTime();
        try {
            ChatResponse response = ollamaInvoker.invocar(prompt);
            long latenciaMs = (System.nanoTime() - t0) / 1_000_000L;

            String texto = response != null && response.getResult() != null
                    ? response.getResult().getOutput().getContent()
                    : null;

            if (texto == null || texto.isBlank()) {
                throw new ChatbotUnavailableException("El modelo devolvió una respuesta vacía");
            }

            if (log.isDebugEnabled()) {
                int hasta = Math.min(LOG_PROMPT_CHARS, systemText.length());
                log.debug("Chatbot respondió en {} ms · prompt[0..{}]={}", latenciaMs, hasta, systemText.substring(0, hasta));
            }

            return texto.trim();
        } catch (ChatbotUnavailableException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            long latenciaMs = (System.nanoTime() - t0) / 1_000_000L;
            log.warn("Chatbot timeout/red tras {} ms: {}", latenciaMs, ex.getMessage());
            throw new ChatbotUnavailableException(
                    "El asistente IA está tardando demasiado en responder. Inténtalo de nuevo en unos segundos.", ex);
        } catch (RuntimeException ex) {
            long latenciaMs = (System.nanoTime() - t0) / 1_000_000L;
            log.warn("Chatbot falló tras {} ms: {}", latenciaMs, ex.getMessage());
            throw new ChatbotUnavailableException("El asistente IA no está disponible en este momento", ex);
        }
    }
}
