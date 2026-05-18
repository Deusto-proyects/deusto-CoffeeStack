package com.deusto.coffeestack.service;

/**
 * Genera el snapshot estructurado de KPIs operativos que el asistente IA
 * recibe como contexto en cada turno de conversación.
 *
 * <p>El snapshot es texto plano en Markdown (en español) que combina:
 * sugerencias de reposición activas, riesgos de faltantes, cobertura crítica,
 * productos más vendidos y productos con baja rotación. No incluye datos
 * personales (emails ni teléfonos de proveedores, ni datos de usuarios).
 */
public interface ContextoNegocioService {

    /**
     * Genera el snapshot Markdown a inyectar en el system prompt del LLM.
     *
     * @return texto Markdown estructurado con secciones {@code ##} por área.
     */
    String generarSnapshot();
}
