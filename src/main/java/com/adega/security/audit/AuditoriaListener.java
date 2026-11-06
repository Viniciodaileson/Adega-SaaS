package com.adega.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaListener {

    private static final Logger REGISTRO = LoggerFactory.getLogger(AuditoriaListener.class);

    private final AuditLogRepository repositorio;

    public AuditoriaListener(AuditLogRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Async
    @EventListener
    public void registrar(AuditLogEvent evento) {
        try {
            repositorio.save(evento.paraEntidade());
        } catch (RuntimeException excecao) {
            // A falha na auditoria não pode derrubar a requisição original,
            // mas precisa ficar visível para a operação.
            REGISTRO.error("Não foi possível gravar o log de auditoria de {} {}.",
                    evento.metodo(), evento.caminho(), excecao);
        }
    }
}
