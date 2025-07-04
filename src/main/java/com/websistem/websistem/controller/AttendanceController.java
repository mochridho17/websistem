package com.websistem.websistem.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.websistem.websistem.model.Attendance;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AttendanceRepository;

@Controller
public class AttendanceController {
    @Autowired
    private AttendanceRepository attendanceRepository;

   @GetMapping("/dataAttendance")
    public String dataAttendance(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) String badgenumber,
        HttpSession session,
        Model model,
        HttpServletRequest request
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        // Validasi range tanggal maksimal 30 hari
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            long days = ChronoUnit.DAYS.between(start, end);
            if (days < 0) {
                model.addAttribute("error", "Tanggal akhir harus setelah tanggal awal.");
                return "dataAttendance";
            }
            if (days > 30) {
                model.addAttribute("error", "Rentang tanggal maksimal 30 hari.");
                return "dataAttendance";
            }
        }

        if (badgenumber != null && badgenumber.trim().isEmpty()) {
            badgenumber = null;
        }

        List<Object[]> data = null;
        if (startDate != null && endDate != null) {
            LocalDateTime startDb = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
            LocalDateTime endDb = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59);
            data = attendanceRepository.findSyncedAttendance(
                startDb, endDb, user.getFactory(), badgenumber
                
            );

            System.out.println("Factory: " + user.getFactory());
            System.out.println("Start: " + startDb + ", End: " + endDb + ", NIK: " + badgenumber);
            System.out.println("Data size: " + data.size());
                    }

        model.addAttribute("attendanceList", data);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("ip", request.getRemoteAddr());
        model.addAttribute("role", user.getRole());

        model.addAttribute("factory", user.getFactory());

        return "dataAttendance";
    }

    @GetMapping("/download-attendance")
    public void downloadAttendance(
        @RequestParam String startDate,
        @RequestParam String endDate,
        @RequestParam(required = false) String badgenumber,
        HttpSession session,
        HttpServletResponse response
    ) throws IOException {
        // Validasi range tanggal maksimal 30 hari
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        long days = ChronoUnit.DAYS.between(start, end);
        if (days < 0 || days > 30) {
            response.setContentType("text/plain");
            response.getWriter().write("Rentang tanggal maksimal 30 hari dan tanggal akhir harus setelah tanggal awal.");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (badgenumber != null && badgenumber.trim().isEmpty()) {
            badgenumber = null;
        }

        LocalDateTime startDb = start.atStartOfDay();
        LocalDateTime endDb = end.atTime(23, 59, 59);

        // Ambil data
        List<Object[]> data = attendanceRepository.findSyncedAttendance(
            startDb, endDb, user.getFactory(), badgenumber
        );

        String fileName = "attendance-" + startDate + "_" + endDate + ".txt";
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        PrintWriter writer = response.getWriter();

        // Map untuk menyimpan data unik per badge per tanggal per menit
        Map<String, List<Object[]>> grouped = new LinkedHashMap<>();

        for (Object[] row : data) {
            String badge = (String) row[0];
            LocalDateTime checktime = (LocalDateTime) row[1];
            String tanggal = checktime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String menit = checktime.format(DateTimeFormatter.ofPattern("HH:mm"));
            String key = badge + ";" + tanggal;

            // Inisialisasi list jika belum ada
            grouped.putIfAbsent(key, new ArrayList<>());

            // Cek apakah sudah ada data dengan menit yang sama
            boolean menitSudahAda = grouped.get(key).stream()
                .anyMatch(r -> ((LocalDateTime) r[1]).format(DateTimeFormatter.ofPattern("HH:mm")).equals(menit));

            // Jika belum ada menit yang sama dan belum 2 data, tambahkan
            if (!menitSudahAda && grouped.get(key).size() < 2) {
                grouped.get(key).add(row);
            }
        }

        // Tulis hasil ke file
        for (List<Object[]> rows : grouped.values()) {
            for (Object[] row : rows) {
                String badge = (String) row[0];
                String badgeFormatted = String.format("%010d", Long.parseLong(badge));
                LocalDateTime checktime = (LocalDateTime) row[1];
                String tanggal = checktime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                String jam = checktime.format(DateTimeFormatter.ofPattern("HH:mm"));
                writer.printf("%s;%s;%s%n", badgeFormatted, tanggal, jam);
            }
        }

        writer.flush();
        writer.close();

        }

}