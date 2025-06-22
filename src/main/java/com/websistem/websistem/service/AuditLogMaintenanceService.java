package com.websistem.websistem.service;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditLogMaintenanceService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Setiap bulan pada tanggal 1 jam 02:00 pagi
    @Scheduled(cron = "0 0 2 1 * ?")
    public void backupAndDeleteOldAuditLogs() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<AuditLog> oldLogs = auditLogRepository.findByTimestampBefore(oneMonthAgo);
        // LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        // List<AuditLog> oldLogs = auditLogRepository.findByTimestampBefore(twoDaysAgo);

        if (!oldLogs.isEmpty()) {
            // Backup ke file CSV
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "D:/backup-auditlog/backup_auditlog_" + timestamp + ".csv";
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println("id,username,ip,action,entityName,entityId,description,timestamp");
                for (AuditLog log : oldLogs) {
                    writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                        log.getId(),
                        log.getUsername(),
                        log.getIp(),
                        log.getAction(),
                        log.getEntityName(),
                        log.getEntityId(),
                        log.getDescription() != null ? log.getDescription().replace(",", " ") : "",
                        log.getTimestamp().toString()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Jika backup gagal, jangan hapus log!
                return;
            }

            // Hapus log lama setelah backup sukses
            auditLogRepository.deleteAll(oldLogs);
        }
    }

}