package com.websistem.websistem.controller;

import com.websistem.websistem.model.User;
import com.websistem.websistem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

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
                        HttpSession session) {
        User user = userService.login(username, password, factory);
        if (user != null) {
            session.setAttribute("user", user);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Username, password, atau factory salah!");
            return "index";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}