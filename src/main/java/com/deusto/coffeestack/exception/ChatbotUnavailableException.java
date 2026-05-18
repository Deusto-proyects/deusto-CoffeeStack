package com.deusto.coffeestack.exception;

/**
 * Lanzada cuando el asistente IA no puede generar una respuesta:
 * Ollama no responde, el modelo no está cargado, timeout, etc.
 *
 * <p>El handler global la mapea a HTTP 503 Service Unavailable.
 */
public class ChatbotUnavailableException extends RuntimeException {

    public ChatbotUnavailableException(String message) {
        super(message);
    }

    public ChatbotUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
