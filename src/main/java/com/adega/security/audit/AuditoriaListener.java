package com.adega.security.audit;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaListener {

    private final AuditLogRepository repositorio;

    public AuditoriaListener(AuditLogRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Async
    @EventListener
    public void registrar(AuditLogEvent evento) {
        repositorio.save(evento.paraEntidade());
    }
}
