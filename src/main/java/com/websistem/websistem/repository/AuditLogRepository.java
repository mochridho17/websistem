package com.websistem.websistem.repository;

import com.websistem.websistem.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { }