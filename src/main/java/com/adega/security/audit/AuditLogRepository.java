package com.adega.security.audit;

import org.springframework.data.repository.Repository;

public interface AuditLogRepository extends Repository<AuditLog, Long> {

    AuditLog save(AuditLog registro);
}
