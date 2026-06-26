package com.gamifiedstudyhub.backend.audit.repository;

import com.gamifiedstudyhub.backend.audit.entity.AuthAuditEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEvent, UUID> {
}
