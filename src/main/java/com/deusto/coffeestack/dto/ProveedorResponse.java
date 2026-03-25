package com.deusto.coffeestack.dto;

public class ProveedorResponse {

    private Long id;
    private String nombre;
    private String contacto;
    private String email;
    private String telefono;
    private boolean activo;

    public ProveedorResponse(Long id, String nombre, String contacto, String email, String telefono, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.email = email;
        this.telefono = telefono;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public boolean isActivo() { return activo; }
}
