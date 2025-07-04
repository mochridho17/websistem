package com.websistem.websistem.controller;

import com.websistem.websistem.service.AttendanceSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sync")
public class AttendanceSyncController {

    @Autowired
    private AttendanceSyncService attendanceSyncService;

    @PostMapping("/manual")
    public String manualSync() {
        try {
            attendanceSyncService.syncData();
            return "Sync berhasil dijalankan!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Sync gagal: " + e.getMessage();
        }
    }

    @GetMapping("/manual")
    public String manualSyncGet() {
        return manualSync();
    }

    @GetMapping("/check-sqlserver")
    public String checkSqlServer() {
        boolean connected = attendanceSyncService.checkSqlServerConnection();
        return connected ? "Koneksi SQL Server: SUKSES" : "Koneksi SQL Server: GAGAL";
    }
}