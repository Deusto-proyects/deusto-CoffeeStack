package com.deusto.coffeestack.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 60, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 60)
    private String updatedBy;

    // ── Getters ─────────────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public RolEnum getRol() { return rol; }

    public boolean isActivo() { return activo; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getCreatedBy() { return createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getUpdatedBy() { return updatedBy; }

    // ── Setters ─────────────────────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setUsername(String username) { this.username = username; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public void setRol(RolEnum rol) { this.rol = rol; }

    public void setActivo(boolean activo) { this.activo = activo; }
}
