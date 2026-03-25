package com.deusto.coffeestack.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit record for every stock change (merma, rotura, or manual adjustment).
 *
 * <p>Business rules enforced at service level:
 * <ul>
 *   <li>MERMA / ROTURA / AJUSTE_NEGATIVO: {@code cantidad} is subtracted from the
 *       batch's {@code cantidadActual}; validated not to exceed available stock.</li>
 *   <li>AJUSTE_POSITIVO: {@code cantidad} is added to {@code cantidadActual}.</li>
 *   <li>A non-blank {@code motivo} is always required.</li>
 * </ul>
 */
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The specific batch affected by this movement. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    /** Classification of the movement (MERMA, ROTURA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private TipoMovimiento tipoMovimiento;

    /** Absolute quantity affected (always positive; direction is implied by tipoMovimiento). */
    @Column(nullable = false)
    private double cantidad;

    /** Mandatory justification or reason for the movement. */
    @Column(nullable = false, length = 300)
    private String motivo;

    /** Login of the authenticated user who registered this movement. */
    @Column(nullable = false, length = 120)
    private String usuario;

    /** UTC timestamp of the movement. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    // ---- getters & setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
