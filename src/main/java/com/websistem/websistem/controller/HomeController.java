package com.websistem.websistem.controller;

import com.websistem.websistem.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @GetMapping("/home")
public String homePage(Model model, HttpSession session, HttpServletRequest request) {
    User loginUser = (User) session.getAttribute("user");
    if (loginUser == null) {
        return "redirect:/login";
    }
    model.addAttribute("username", loginUser.getUsername());
    model.addAttribute("factory", loginUser.getFactory());
    model.addAttribute("role", loginUser.getRole());
    model.addAttribute("ip", request.getRemoteAddr());
    return "home";
}
}