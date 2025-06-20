package com.websistem.websistem.repository;

import com.websistem.websistem.model.AuditLog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { 
    List<AuditLog> findAllByOrderByTimestampDesc();
 }