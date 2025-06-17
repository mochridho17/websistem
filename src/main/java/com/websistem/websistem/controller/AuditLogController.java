package com.websistem.websistem.controller;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepository;

   @GetMapping("/audit-log")
public String showAuditLog(Model model, HttpSession session, HttpServletRequest request) {
    List<AuditLog> logs = auditLogRepository.findAll();
    if (logs == null) {
        logs = new ArrayList<>();
    }
    model.addAttribute("logs", logs);

    User loginUser = (User) session.getAttribute("user");
    String ip = request.getRemoteAddr();

    AuditLog log = new AuditLog();
    log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
    log.setIp(ip);
    // // ...set field lain...
    // auditLogRepository.save(log);
    
    model.addAttribute("username", loginUser != null ? loginUser.getUsername() : "-");
    model.addAttribute("ip", request.getRemoteAddr());
    // Tambahkan baris ini:
    model.addAttribute("factory", loginUser != null ? loginUser.getFactory() : "-");
    model.addAttribute("role", loginUser != null ? loginUser.getRole() : "-");

    return "auditLog";
}
}