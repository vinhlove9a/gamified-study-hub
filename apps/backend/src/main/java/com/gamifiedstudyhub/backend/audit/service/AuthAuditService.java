package com.gamifiedstudyhub.backend.audit.service;

import com.gamifiedstudyhub.backend.audit.AuthEventType;
import com.gamifiedstudyhub.backend.audit.entity.AuthAuditEvent;
import com.gamifiedstudyhub.backend.audit.repository.AuthAuditEventRepository;
import com.gamifiedstudyhub.backend.common.util.DateTimeUtils;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Records auth/authz events to the append-only audit trail. Writes are
 * asynchronous so they never block (or fail) the request, and swallow errors
 * to keep auditing strictly best-effort.
 */
@Service
public class AuthAuditService {

    private static final Logger log = LoggerFactory.getLogger(AuthAuditService.class);

    private final AuthAuditEventRepository repository;

    public AuthAuditService(AuthAuditEventRepository repository) {
        this.repository = repository;
    }

    @Async("auditExecutor")
    public void record(AuthEventType type, UUID userId, RequestMetadata meta, String metadata) {
        try {
            AuthAuditEvent event = new AuthAuditEvent();
            event.setEventType(type.name());
            event.setUserId(userId);
            if (meta != null) {
                event.setIp(meta.ip());
                event.setUserAgent(meta.userAgent());
            }
            event.setMetadata(metadata);
            event.setCreatedAt(DateTimeUtils.nowUtc());
            repository.save(event);
        } catch (RuntimeException e) {
            log.warn("Failed to record audit event {} (ignored): {}", type, e.getMessage());
        }
    }

    public void record(AuthEventType type, UUID userId, RequestMetadata meta) {
        record(type, userId, meta, null);
    }
}
