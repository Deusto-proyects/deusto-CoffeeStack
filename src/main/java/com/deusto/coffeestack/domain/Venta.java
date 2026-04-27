package com.deusto.coffeestack.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Venta registra la cabecera de una transacción de venta.
 *
 * <p>Cada venta es generada por un empleado autenticado y contiene
 * una o más {@link VentaLinea líneas de detalle}, una por cada
 * producto ({@link Item}) vendido.
 */
@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login del usuario que registró la venta. */
    @Column(nullable = false, length = 120)
    private String usuario;

    /** Momento en que se registró la venta. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Líneas de detalle de la venta (un registro por producto vendido). */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaLinea> lineas = new ArrayList<>();

    // ---- getters & setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public List<VentaLinea> getLineas() { return lineas; }
    public void setLineas(List<VentaLinea> lineas) { this.lineas = lineas; }
}
