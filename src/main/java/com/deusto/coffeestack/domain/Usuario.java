package com.deusto.coffeestack.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolEnum rol;

    @Column(nullable = false)
    private boolean activo = true;

    // ── Getters ─────────────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public RolEnum getRol() { return rol; }

    public boolean isActivo() { return activo; }

    // ── Setters ─────────────────────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setUsername(String username) { this.username = username; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public void setRol(RolEnum rol) { this.rol = rol; }

    public void setActivo(boolean activo) { this.activo = activo; }
}
