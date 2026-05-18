package com.deusto.coffeestack.controller;

import com.deusto.coffeestack.dto.PreguntaChatbotRequest;
import com.deusto.coffeestack.dto.RespuestaChatbotResponse;
import com.deusto.coffeestack.service.ChatbotService;
import com.deusto.coffeestack.service.ContextoNegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Endpoint del asistente IA conversacional. Responde a preguntas operativas
 * de la cafetería usando el snapshot de KPIs como contexto.
 *
 * <p>Acceso restringido a PROPIETARIO y ROOT: las decisiones sobre compras,
 * márgenes y rotación están vinculadas al rol de gestión.
 */
@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Asistente IA", description = "Chatbot operativo basado en LLM local (Ollama)")
@SecurityRequirement(name = "basicAuth")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ContextoNegocioService contextoNegocioService;

    public ChatbotController(ChatbotService chatbotService,
                             ContextoNegocioService contextoNegocioService) {
        this.chatbotService = chatbotService;
        this.contextoNegocioService = contextoNegocioService;
    }

    @PostMapping("/preguntar")
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ROOT')")
    @Operation(summary = "Enviar una pregunta al asistente IA y obtener una respuesta")
    public RespuestaChatbotResponse preguntar(@Valid @RequestBody PreguntaChatbotRequest request) {
        long t0 = System.nanoTime();
        String respuesta = chatbotService.responder(request.getPregunta());
        long latenciaMs = (System.nanoTime() - t0) / 1_000_000L;
        return new RespuestaChatbotResponse(respuesta, Instant.now(), latenciaMs);
    }

    /**
     * Devuelve, en texto plano, el snapshot exacto que el asistente recibe
     * como contexto en cada pregunta. Útil para depurar el prompt y entender
     * por qué el modelo responde como lo hace.
     */
    @GetMapping(value = "/snapshot", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ROOT')")
    @Operation(summary = "Inspeccionar el snapshot de negocio que recibe el asistente")
    public String snapshot() {
        return contextoNegocioService.generarSnapshot();
    }
}
