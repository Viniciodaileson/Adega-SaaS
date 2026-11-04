package com.adega.security.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "logs_auditoria")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assunto_usuario", length = 120)
    private String assuntoUsuario;

    @Column(nullable = false, length = 10)
    private String metodo;

    @Column(nullable = false, length = 255)
    private String caminho;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "status_resposta", nullable = false)
    private int statusResposta;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private Instant registradoEm;

    protected AuditLog() {
    }

    public AuditLog(String assuntoUsuario,
                    String metodo,
                    String caminho,
                    String ipOrigem,
                    int statusResposta,
                    Instant registradoEm) {
        this.assuntoUsuario = assuntoUsuario;
        this.metodo = metodo;
        this.caminho = caminho;
        this.ipOrigem = ipOrigem;
        this.statusResposta = statusResposta;
        this.registradoEm = registradoEm;
    }

    public Long getId() {
        return id;
    }

    public String getAssuntoUsuario() {
        return assuntoUsuario;
    }

    public String getMetodo() {
        return metodo;
    }

    public String getCaminho() {
        return caminho;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public int getStatusResposta() {
        return statusResposta;
    }

    public Instant getRegistradoEm() {
        return registradoEm;
    }
}
