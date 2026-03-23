package com.deusto.coffeestack.domain;

import jakarta.persistence.*;

/**
 * Insumo (ingredient/supply) managed in the coffee shop inventory.
 */
@Entity
@Table(name = "insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Unit of measure, e.g. "kg", "litros", "unidades" */
    @Column(name = "unidad_medida", nullable = false, length = 30)
    private String unidadMedida;

    /**
     * Minimum stock threshold. When total current quantity falls below this value
     * the system considers there is a shortage risk ("riesgo de faltante").
     */
    @Column(name = "stock_minimo_alerta", nullable = false)
    private double stockMinimoAlerta;

    // ---- getters & setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public double getStockMinimoAlerta() { return stockMinimoAlerta; }
    public void setStockMinimoAlerta(double stockMinimoAlerta) { this.stockMinimoAlerta = stockMinimoAlerta; }
}
