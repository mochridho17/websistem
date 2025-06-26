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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        // Validasi: hanya huruf besar, angka, titik, strip, tanpa spasi
        if (!username.matches("[A-Z0-9.-]+")) {
            model.addAttribute("error", "Username harus huruf besar semua, boleh angka, titik (.) atau strip (-), tanpa spasi!");
            return "index";
        }

        // Cari user tanpa factory jika SUPERDEV
        User user = userService.findByUsernameAndFactory(username, factory);
        if (user == null) {
            // Coba cari user SUPER_DEV tanpa factory
            user = userService.findByUsernameAndRole(username, "SUPER_DEV");
        }

        if (user != null) {
            boolean isSuperDev = "SUPER_DEV".equals(user.getRole());
            if (isSuperDev) {
                // Reset blokir dan counter setiap login
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                userService.save(user);
            }

            if (!isSuperDev && user.isAccountLocked()) {
                model.addAttribute("error", "Akun Anda diblokir karena 5x gagal login. Hubungi IT untuk membuka blokir.");
                return "index";
            }

            // Login: factory diabaikan jika SUPER_DEV
            User loginUser = isSuperDev
                ? userService.login(username, password) // Buat method login(username, password) di UserService
                : userService.login(username, password, factory);

            if (loginUser != null) {
                // Reset counter gagal login
                user.setFailedLoginAttempts(0);
                userService.save(user);

                session.setAttribute("user", loginUser);

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
                // Tambah counter gagal login (kecuali SUPER_DEV)
                if (!isSuperDev) {
                    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                    if (user.getFailedLoginAttempts() >= 5) {
                        user.setAccountLocked(true);
                    }
                    userService.save(user);
                    if (user.isAccountLocked()) {
                        model.addAttribute("error", "Akun Anda diblokir karena 5x gagal login. Hubungi IT untuk membuka blokir.");
                    } else {
                        model.addAttribute("error", "Username, password, atau factory salah!");
                    }
                } else {
                    model.addAttribute("error", "Username atau password salah!");
                }
                return "index";
            }
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

    @PostMapping("/unlock-user/{id}")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser != null && "ADMIN".equals(loginUser.getRole())) {
            User user = userService.findById(id);
            if (user != null) {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                userService.save(user);
                redirectAttributes.addFlashAttribute("success", "Akun berhasil di-unlock.");
            }
        }
        return "redirect:/dataUsers";
    }
}