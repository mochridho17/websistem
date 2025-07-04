package com.websistem.websistem.service;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditLogMaintenanceService {

    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private JavaMailSender mailSender;

    // Setiap tanggal 1 jam 09:00 pagi
    @Scheduled(cron = "0 0 9 1 * ?")
    public void backupAndDeleteOldAuditLogs() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<AuditLog> oldLogs = auditLogRepository.findByTimestampBefore(oneMonthAgo);

        if (oldLogs.isEmpty()) {
            // Tidak ada data lama, tidak perlu backup/hapus
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "D:/backup-auditlog/backup_auditlog_" + timestamp + ".csv";
        boolean backupSuccess = false;

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
            backupSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            // Kirim email error jika backup gagal
            SimpleMailMessage errorMsg = new SimpleMailMessage();
            errorMsg.setFrom("it.ridho@pt-richshoes.com");
            errorMsg.setTo("it.ridho@pt-richshoes.com");
            errorMsg.setSubject("Backup Audit Log GAGAL");
            errorMsg.setText("Backup audit log gagal pada " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                "\nError: " + e.getMessage());
            mailSender.send(errorMsg);
            return;
        }

        // Hapus log lama setelah backup sukses
        if (backupSuccess) {
            auditLogRepository.deleteAll(oldLogs);

            // Kirim email notifikasi sukses
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("it.ridho@pt-richshoes.com");
            message.setTo("it.ridho@pt-richshoes.com");
            message.setSubject("Backup Audit Log Selesai");
            message.setText("Backup audit log selesai.\nFile: " + fileName +
                "\nJumlah log dibackup: " + oldLogs.size() +
                "\nWaktu: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            mailSender.send(message);
        }
    }

    public void restoreAuditLogFromBackup(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                AuditLog log = new AuditLog();
                // JANGAN setId! Biarkan database yang generate
                log.setUsername(parts[1]);
                log.setIp(parts[2]);
                log.setAction(parts[3]);
                log.setEntityName(parts[4]);
                log.setEntityId(parts[5]);
                log.setDescription(parts[6]);
                log.setTimestamp(LocalDateTime.parse(parts[7]));
                auditLogRepository.save(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}