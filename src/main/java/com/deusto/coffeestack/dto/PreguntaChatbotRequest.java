package com.deusto.coffeestack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Pregunta enviada al asistente IA.
 *
 * <p>El backend no mantiene historial: cada pregunta se procesa de forma
 * independiente sobre el snapshot de KPIs del momento. Si más adelante se
 * quisiera memoria conversacional, habría que ampliar este DTO con el
 * historial de turnos.
 */
public class PreguntaChatbotRequest {

    @NotBlank(message = "La pregunta no puede estar vacía")
    @Size(max = 500, message = "La pregunta no puede superar los 500 caracteres")
    private String pregunta;

    public PreguntaChatbotRequest() {
    }

    public PreguntaChatbotRequest(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }
}
