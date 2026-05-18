package com.deusto.coffeestack.service;

/**
 * Asistente IA conversacional. Recibe una pregunta en lenguaje natural y
 * devuelve una respuesta razonada basándose exclusivamente en el snapshot
 * de KPIs operativos de la cafetería (no consulta la base de datos por
 * pregunta — la información disponible está fijada por
 * {@link ContextoNegocioService}).
 *
 * <p>La conversación es stateless: cada llamada es independiente.
 */
public interface ChatbotService {

    /**
     * Responde a una pregunta del usuario.
     *
     * @param pregunta texto de la pregunta (no nulo, no vacío)
     * @return respuesta generada por el modelo
     * @throws com.deusto.coffeestack.exception.ChatbotUnavailableException
     *         si el modelo no responde o falla la llamada
     */
    String responder(String pregunta);
}
