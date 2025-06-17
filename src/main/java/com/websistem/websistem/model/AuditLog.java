package com.websistem.websistem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String action;
    private String entityName;
    private String entityId;
    private String description;
    private LocalDateTime timestamp;
    private String ip;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
   public String getTimestampFormatted() {
    if (timestamp == null) return "";
    return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}