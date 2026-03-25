package com.deusto.coffeestack.domain;

/**
 * Classifies cada movimiento de inventario.
 *
 * <ul>
 *   <li>{@link #MERMA}            – pérdida por caducidad, mala conservación, etc.</li>
 *   <li>{@link #ROTURA}           – pérdida por daño físico o accidente.</li>
 *   <li>{@link #AJUSTE_POSITIVO}  – corrección al alza tras recuento físico.</li>
 *   <li>{@link #AJUSTE_NEGATIVO}  – corrección a la baja tras recuento físico.</li>
 * </ul>
 */
public enum TipoMovimiento {
    MERMA,
    ROTURA,
    AJUSTE_POSITIVO,
    AJUSTE_NEGATIVO
}
