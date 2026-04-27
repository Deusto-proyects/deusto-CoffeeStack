package com.deusto.coffeestack.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta completa de una venta registrada.
 */
public class VentaResponse {

    private Long id;
    private String usuario;
    private LocalDateTime fechaHora;
    private List<VentaLineaResponse> lineas;

    public VentaResponse(Long id, String usuario, LocalDateTime fechaHora,
                         List<VentaLineaResponse> lineas) {
        this.id = id;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.lineas = lineas;
    }

    // ---- getters ----

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public List<VentaLineaResponse> getLineas() { return lineas; }
}
