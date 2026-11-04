package com.adega.security.user;

import com.adega.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "hash_senha", nullable = false, length = 255)
    private String hashSenha;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(nullable = false)
    private boolean ativo;

    protected Usuario() {
    }

    public Usuario(String login, String hashSenha) {
        setLogin(login);
        setHashSenha(hashSenha);
        this.criadoEm = Instant.now();
        this.ativo = true;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {

        if (login == null || login.isBlank()) {
            throw new RegraNegocioException("O login não pode ser vazio.");
        }

        String normalizado = login.trim().toLowerCase(Locale.ROOT);

        if (normalizado.length() > 60) {
            throw new RegraNegocioException("O login deve ter no máximo 60 caracteres.");
        }

        this.login = normalizado;
    }

    public String getHashSenha() {
        return hashSenha;
    }

    public void setHashSenha(String hashSenha) {

        if (hashSenha == null || hashSenha.isBlank()) {
            throw new RegraNegocioException("O hash da senha não pode ser vazio.");
        }

        this.hashSenha = hashSenha;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
