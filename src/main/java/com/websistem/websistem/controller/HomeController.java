package com.websistem.websistem.controller;

import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AttendanceRepository;
import com.websistem.websistem.repository.EmployeeFiwaRepository;
import com.websistem.websistem.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final AttendanceRepository attendanceRepository;

    private final EmployeeFiwaRepository employeeFiwaRepository;

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository, EmployeeFiwaRepository employeeFiwaRepository, AttendanceRepository attendanceRepository) {
    this.userRepository = userRepository;
    this.employeeFiwaRepository = employeeFiwaRepository;
    this.attendanceRepository = attendanceRepository;
}

    @GetMapping("/home")
    public String homePage(Model model, HttpSession session, HttpServletRequest request) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:/login";
        }
        long totalUser = userRepository.count();
        // Hitung total kehadiran berdasarkan factory user yang login
        long totalAttendance = attendanceRepository.countByFactory(loginUser.getFactory());
         // Hitung total karyawan dan kehadiran berdasarkan factory user yang login
        long totalEmployee = employeeFiwaRepository.countByFactory(loginUser.getFactory());


        model.addAttribute("totalUser", totalUser);
        model.addAttribute("totalEmployee", totalEmployee);
        model.addAttribute("totalAttendance", totalAttendance);

        model.addAttribute("username", loginUser.getUsername());
        model.addAttribute("factory", loginUser.getFactory());
        model.addAttribute("role", loginUser.getRole());
        model.addAttribute("ip", request.getRemoteAddr());
        return "home";
    }
}