package com.websistem.websistem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class AttendanceSyncService {

    @Autowired
    private JavaMailSender mailSender;

    // Simpan waktu sync terakhir (bisa juga di DB)
    private LocalDateTime lastSyncTime = LocalDateTime.now().minusDays(1);

    public boolean checkSqlServerConnection() {
        String sqlServerUrl = "jdbc:sqlserver://localhost:1433;databaseName=dbfm;user=ADMIN;password=Admin2025;encrypt=true;trustServerCertificate=true";
        try (Connection conn = DriverManager.getConnection(sqlServerUrl)) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Scheduled(cron = "0 30 8 * * ?") // setiap hari jam 08:30 pagi
    public void syncData() {
        String sqlServerUrl = "jdbc:sqlserver://localhost:1433;databaseName=dbfm;user=ADMIN;password=Admin2025;encrypt=true;trustServerCertificate=true";
        String pgUrl = "jdbc:postgresql://localhost:5433/postgres";
        String pgUser = "postgres";
        String pgPass = "admin2025";

        try (
            Connection sqlConn = DriverManager.getConnection(sqlServerUrl);
            Connection pgConn = DriverManager.getConnection(pgUrl, pgUser, pgPass)
        ) {
            // Ambil semua data dari SQL Server
            String query = "SELECT U.BADGENUMBER, U.NAME, C.CHECKTIME " +
                        "FROM USERINFO U JOIN CHECKINOUT C ON U.USERID = C.USERID";
            PreparedStatement stmt = sqlConn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Insert ke PostgreSQL, abaikan jika sudah ada (ON CONFLICT)
            PreparedStatement ps = pgConn.prepareStatement(
                "INSERT INTO attendance_log (badgenumber, name, checktime) VALUES (?, ?, ?) " +
                "ON CONFLICT (badgenumber, checktime) DO NOTHING"
            );

            int count = 0;
            while (rs.next()) {
                ps.setString(1, rs.getString("BADGENUMBER"));
                ps.setString(2, rs.getString("NAME"));
                ps.setTimestamp(3, rs.getTimestamp("CHECKTIME"));
                ps.executeUpdate();
                count++;
            }
            rs.close();
            stmt.close();
            ps.close();

            System.out.println("Sync selesai, data baru: " + count);

            // Format tanggal dan jam
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // Kirim email notifikasi
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("it.ridho@pt-richshoes.com");
            message.setTo("it.ridho@pt-richshoes.com"); // penerima WAJIB valid
            message.setSubject("Sync Absensi Selesai");
            message.setText("Sync absensi selesai. Data baru: " + count +
                " pada " + LocalDateTime.now().format(formatter));

            System.out.println("Mail username: " + mailSender);
            System.out.println("To: " + Arrays.toString(message.getTo()));

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}