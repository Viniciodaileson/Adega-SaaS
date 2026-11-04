package com.adega.security.audit;

import java.time.Instant;

public record AuditLogEvent(
        String assuntoUsuario,
        String metodo,
        String caminho,
        String ipOrigem,
        int statusResposta,
        Instant registradoEm
) {

    public AuditLog paraEntidade() {
        return new AuditLog(assuntoUsuario, metodo, caminho, ipOrigem, statusResposta, registradoEm);
    }
}
