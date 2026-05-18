package com.deusto.coffeestack.dto;

import java.time.Instant;

/**
 * Respuesta del asistente IA a una pregunta, junto con metadatos de la llamada
 * (timestamp de la respuesta y latencia total en milisegundos).
 */
public class RespuestaChatbotResponse {

    private String respuesta;
    private Instant timestamp;
    private long latenciaMs;

    public RespuestaChatbotResponse() {
    }

    public RespuestaChatbotResponse(String respuesta, Instant timestamp, long latenciaMs) {
        this.respuesta = respuesta;
        this.timestamp = timestamp;
        this.latenciaMs = latenciaMs;
    }

    public String getRespuesta() { return respuesta; }
    public Instant getTimestamp() { return timestamp; }
    public long getLatenciaMs() { return latenciaMs; }
}
