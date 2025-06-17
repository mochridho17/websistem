package com.websistem.websistem.controller;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AuditLogRepository;
import com.websistem.websistem.service.UserService;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    private UserService userService;

    LoginController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

   @GetMapping("/")
public String showLoginForm(HttpSession session, Model model, HttpServletRequest request) {
    if (session.getAttribute("user") != null) {
        return "redirect:/home";
    }
    String ip = request.getRemoteAddr();
    model.addAttribute("ip", ip);
    return "index";
}

    @GetMapping("/login")
    public String redirectLoginToIndex() {
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String factory,
                        Model model,
                        HttpSession session,
                        HttpServletRequest request) {
        User user = userService.login(username, password, factory);
        if (user != null) {
            session.setAttribute("user", user);

            // Audit log login
            AuditLog log = new AuditLog();
            log.setUsername(user.getUsername());
            log.setIp(request.getRemoteAddr());
            log.setAction("LOGIN");
            log.setEntityName("User");
            log.setEntityId(String.valueOf(user.getId()));
            log.setDescription("Login user: " + user.getUsername());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);

            return "redirect:/home";
        } else {
            model.addAttribute("error", "Username, password, atau factory salah!");
            return "index";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        User loginUser = (User) session.getAttribute("user");

        if (loginUser != null) {
            AuditLog log = new AuditLog();
            log.setUsername(loginUser.getUsername());
            log.setIp(request.getRemoteAddr());
            log.setAction("LOGOUT");
            log.setEntityName("User");
            log.setEntityId(String.valueOf(loginUser.getId()));
            log.setDescription("Logout user: " + loginUser.getUsername());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        }

        session.invalidate();
        return "redirect:/";
    }
}