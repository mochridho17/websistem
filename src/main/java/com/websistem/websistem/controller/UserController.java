package com.websistem.websistem.controller;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.UserRepository;
import com.websistem.websistem.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/dataUsers")
    public String showUserPage(Model model, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
    User loginUser = (User) session.getAttribute("user");
    if (loginUser == null) {
        redirectAttributes.addFlashAttribute("error", "Silakan login terlebih dahulu.");
        return "redirect:/login";
    }
    if (!"ADMIN".equals(loginUser.getRole())) {
        redirectAttributes.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini.");
        return "redirect:/home";
    }

    User userForm = new User();
    userForm.setCreatedPerson(loginUser.getUsername());
    model.addAttribute("user", userForm);

    model.addAttribute("username", loginUser.getUsername());
    model.addAttribute("factory", loginUser.getFactory());
    model.addAttribute("ip", request.getRemoteAddr());
    model.addAttribute("userList", userRepository.findAll());
    model.addAttribute("role", loginUser.getRole());

    return "dataUsers";
}

    @PostMapping("/dataUsers")
    public String queryUsers(@ModelAttribute("user") User user, Model model, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "Silakan login terlebih dahulu.");
            return "redirect:/login";
        }
        // Hanya ADMIN yang bisa query user
        if (!"ADMIN".equals(loginUser.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini.");
            return "redirect:/home";
        }
        model.addAttribute("username", loginUser.getUsername());
        model.addAttribute("factory", loginUser.getFactory());
        model.addAttribute("ip", request.getRemoteAddr());
        model.addAttribute("role", loginUser.getRole());
       

        // Query by NIK (username) jika diisi, jika kosong tampilkan semua
        List<User> userList;
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            userList = userRepository.findByUsername(user.getUsername().trim());
        } else {
            userList = userRepository.findAll();
        }
        model.addAttribute("userList", userList);
        model.addAttribute("user", new User());
        return "dataUsers";
    }

    @PostMapping("/add-user")
    public String addUser(@ModelAttribute User user, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Cek duplikasi username di factory yang sama
        String username = user.getUsername().toUpperCase();
        if (!username.matches("[A-Z0-9.-]+")) {
            redirectAttributes.addFlashAttribute("error", "Username hanya boleh huruf, angka, titik (.) atau strip (-) dan tanpa spasi!");
            return "redirect:/dataUsers";
        }
        user.setUsername(username);
        user.setUsername(user.getUsername().toUpperCase()); // UPPERCASE
        List<User> existingUsers = userRepository.findByUsernameAndFactory(user.getUsername(), user.getFactory());
        if (!existingUsers.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User dengan username dan factory yang sama sudah ada!");
            return "redirect:/dataUsers";
        }

        userRepository.save(user);

        User loginUser = (User) session.getAttribute("user");
        AuditLog log = new AuditLog();
        log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
        log.setIp(request.getRemoteAddr());
        log.setAction("ADD_USER");
        log.setEntityName("User");
        log.setEntityId(String.valueOf(user.getId()));
        log.setDescription("Tambah user: " + user.getUsername());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        redirectAttributes.addFlashAttribute("success", "User berhasil ditambah!");
        return "redirect:/dataUsers";
    }

    @PostMapping("/edit-user")
    public String editUser(@ModelAttribute User user, Model model, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User existing = userRepository.findById(user.getId()).orElse(null);
        if (existing != null) {
            existing.setUsername(user.getUsername());
            existing.setPassword(user.getPassword());
            existing.setFactory(user.getFactory());
            existing.setAuthority(user.getAuthority()); // <-- Tambahkan baris ini!
            // createdPerson tidak diubah
            userRepository.save(existing);

            // Audit log ...
            User loginUser = (User) session.getAttribute("user");
            AuditLog log = new AuditLog();
            log.setIp(request.getRemoteAddr());
            log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
            log.setAction("EDIT_USER");
            log.setEntityName("User");
            log.setEntityId(String.valueOf(user.getId()));
            log.setDescription("Edit user: " + user.getUsername());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);

            redirectAttributes.addFlashAttribute("success", "User berhasil diupdate!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User tidak ditemukan!");
        }
        return "redirect:/dataUsers";
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                userRepository.delete(user);

                User loginUser = (User) session.getAttribute("user");
                AuditLog log = new AuditLog();
                log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
                log.setIp(request.getRemoteAddr());
                log.setAction("DELETE_USER");
                log.setEntityName("User");
                log.setEntityId(String.valueOf(user.getId()));
                log.setDescription("Hapus user: " + user.getUsername());
                log.setTimestamp(LocalDateTime.now());
                auditLogRepository.save(log);

                redirectAttributes.addFlashAttribute("success", "User berhasil dihapus!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User tidak ditemukan!");
            }
            return "redirect:/dataUsers";
        }

    @GetMapping("/delete-user/{id}")
    public String redirectDeleteUser() {
        return "redirect:/dataUsers";
    }

}