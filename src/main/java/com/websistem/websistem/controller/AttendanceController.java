package com.websistem.websistem.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
    @RequestParam(required = false) String employeeNo,
    HttpSession session,
    Model model,
    HttpServletRequest request
) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/";
    }
    String factory = user.getFactory();

    String startDb = (startDate != null && !startDate.isEmpty()) ? startDate.replace("-", "/") : null;
    String endDb = (endDate != null && !endDate.isEmpty()) ? endDate.replace("-", "/") : null;

    if (employeeNo != null && employeeNo.trim().isEmpty()) {
        employeeNo = null;
    }

    List<Attendance> data = null;
    if (startDb != null && endDb != null) {
        data = attendanceRepository.findByTanggalBetweenAndFactory(startDb, endDb, factory, employeeNo);
    }

    model.addAttribute("attendanceList", data);
    model.addAttribute("username", user.getUsername());
    model.addAttribute("factory", factory);
    model.addAttribute("ip", request.getRemoteAddr());
    model.addAttribute("role", user.getRole());

    return "dataAttendance";
}

   @GetMapping("/download-attendance")
public void downloadAttendance(
    @RequestParam String startDate,
    @RequestParam String endDate,
    @RequestParam(required = false) String employeeNo,
    HttpSession session,
    HttpServletResponse response) throws IOException {

    User user = (User) session.getAttribute("user");
    String factory = (user != null) ? user.getFactory() : null;
    String startDb = startDate.replace("-", "/");
    String endDb = endDate.replace("-", "/");

    if (employeeNo != null && employeeNo.trim().isEmpty()) {
        employeeNo = null;
    }

    List<Attendance> data = attendanceRepository.findByTanggalBetweenAndFactory(
        startDb, endDb, factory, employeeNo
    );

    String fileFactory = (factory != null && !factory.isEmpty()) ? factory : "ALL";
    String fileName = fileFactory + "-" + startDate + "_" + endDate + ".txt";

    // --- Kirim file ke browser user seperti biasa ---
    response.setContentType("text/plain");
    response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

    PrintWriter writer = response.getWriter();
    for (Attendance att : data) {
        String nik = String.format("%010d", Long.parseLong(att.getEmployeeNik()));
        writer.printf("%s;%s;%s%n", nik, att.getTanggal(), att.getJam());
    }
    writer.flush();
    writer.close();
}
}