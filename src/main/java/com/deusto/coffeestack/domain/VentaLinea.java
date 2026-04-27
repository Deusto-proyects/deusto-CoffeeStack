package com.deusto.coffeestack.domain;

import jakarta.persistence.*;

/**
 * VentaLinea representa un renglón de una {@link Venta}:
 * qué {@link Item} se vendió y cuántas unidades.
 *
 * <p>El descuento en inventario se realiza a nivel de servicio
 * siguiendo la receta del item y el orden FIFO de lotes.
 */
@Entity
@Table(name = "venta_lineas")
public class VentaLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Venta a la que pertenece esta línea. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    /** Producto (ítem de carta) vendido. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** Número de unidades del producto vendidas en esta línea. */
    @Column(name = "cantidad_unidades", nullable = false)
    private int cantidadUnidades;

    // ---- getters & setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public int getCantidadUnidades() { return cantidadUnidades; }
    public void setCantidadUnidades(int cantidadUnidades) { this.cantidadUnidades = cantidadUnidades; }
}
