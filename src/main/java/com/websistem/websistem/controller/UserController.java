package com.websistem.websistem.controller;

import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dataUsers")
    public String showUserPage(Model model, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "Silakan login terlebih dahulu.");
            return "redirect:/login";
        }
        // Hanya ADMIN yang bisa akses dataUsers
        if (!"ADMIN".equals(loginUser.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini.");
            return "redirect:/home";
        }
        
        model.addAttribute("user", new User());
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
    public String addUser(@ModelAttribute("user") User user, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser != null) {
            user.setCreatedPerson(loginUser.getUsername());
        } else {
            user.setCreatedPerson("SYSTEM");
        }
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "User berhasil ditambahkan!");
        return "redirect:/dataUsers";
    }

    @PostMapping("/edit-user")
    public String editUser(@ModelAttribute User user, Model model, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User existing = userRepository.findById(user.getId()).orElse(null);
        if (existing != null) {
            existing.setUsername(user.getUsername());
            existing.setPassword(user.getPassword());
            existing.setFactory(user.getFactory());
            // createdPerson tidak diubah
            userRepository.save(existing);
            redirectAttributes.addFlashAttribute("success", "User berhasil diupdate!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User tidak ditemukan!");
        }
        return "redirect:/dataUsers";
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "User berhasil dihapus!");
        return "redirect:/dataUsers";
    }

}