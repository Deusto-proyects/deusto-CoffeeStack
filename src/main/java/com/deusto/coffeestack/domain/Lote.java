package com.deusto.coffeestack.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Lote represents a specific batch of an {@link Insumo}.
 * Each batch tracks its current available quantity and optional expiry date.
 */
@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(name = "numero_lote", nullable = false, length = 60)
    private String numeroLote;

    /** Quantity received for this batch (informational). */
    @Column(name = "cantidad_inicial", nullable = false)
    private double cantidadInicial;

    /** Remaining usable quantity in this batch. */
    @Column(name = "cantidad_actual", nullable = false)
    private double cantidadActual;

    /**
     * Expiry date (nullable – not all supplies expire).
     * Technical assumption: a batch with a past expiry date is still
     * counted but callers may choose to exclude it from "usable" stock.
     */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    // ---- getters & setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }

    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }

    public double getCantidadInicial() { return cantidadInicial; }
    public void setCantidadInicial(double cantidadInicial) { this.cantidadInicial = cantidadInicial; }

    public double getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(double cantidadActual) { this.cantidadActual = cantidadActual; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    /** Convenience: returns true when this batch still has stock available. */
    public boolean tieneStock() {
        return cantidadActual > 0;
    }
}
