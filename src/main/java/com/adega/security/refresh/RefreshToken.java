package com.adega.security.refresh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_usuario", columnList = "usuario_id")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "hash_token", nullable = false, unique = true, length = 64)
    private String hashToken;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(nullable = false)
    private boolean revogado;

    @Column(name = "substituido_por", length = 64)
    private String substituidoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected RefreshToken() {
    }

    public RefreshToken(Long usuarioId, String hashToken, Instant expiraEm) {
        this.usuarioId = usuarioId;
        this.hashToken = hashToken;
        this.expiraEm = expiraEm;
        this.revogado = false;
        this.criadoEm = Instant.now();
    }

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getHashToken() {
        return hashToken;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public boolean isRevogado() {
        return revogado;
    }

    public void setRevogado(boolean revogado) {
        this.revogado = revogado;
    }

    public String getSubstituidoPor() {
        return substituidoPor;
    }

    public void setSubstituidoPor(String substituidoPor) {
        this.substituidoPor = substituidoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
