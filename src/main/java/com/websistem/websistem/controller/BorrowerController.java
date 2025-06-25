package com.websistem.websistem.controller;

import com.websistem.websistem.model.Borrower;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.BorrowerRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/borrower")
public class BorrowerController {

    @Autowired
    private BorrowerRepository borrowerRepository;

    @GetMapping
    public String listBorrower(Model model, HttpSession session, HttpServletRequest request) {
        User loginUser = (User) session.getAttribute("user");
        String factory = loginUser != null ? loginUser.getFactory() : null;
        String username = loginUser != null ? loginUser.getUsername() : null;
        String role = loginUser != null ? loginUser.getRole() : null;
        boolean canCrudEmployee = loginUser != null && "CRUD_EMPLOYEE".equals(loginUser.getAuthority());

        model.addAttribute("borrowers", null); // Tidak ada data awal
        model.addAttribute("borrowerPage", null);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        model.addAttribute("pageSize", 10); // default 5 per halaman
        model.addAttribute("keyword", null);

        model.addAttribute("username", username);
        model.addAttribute("factory", factory);
        model.addAttribute("role", role);
        model.addAttribute("canCrudEmployee", canCrudEmployee);
        model.addAttribute("ip", request.getRemoteAddr());
        return "borrowerList";
    }

    @PostMapping("/add")
    public String addBorrower(@ModelAttribute Borrower borrower, RedirectAttributes redirectAttributes) {
        borrowerRepository.save(borrower);
        redirectAttributes.addFlashAttribute("success", "Peminjam berhasil ditambahkan!");
        return "redirect:/borrower";
    }

    @PostMapping("/edit")
    public String editBorrower(@ModelAttribute Borrower borrower, RedirectAttributes redirectAttributes) {
        borrowerRepository.save(borrower);
        redirectAttributes.addFlashAttribute("success", "Peminjam berhasil diubah!");
        return "redirect:/borrower";
    }

    @PostMapping("/delete/{id}")
    public String deleteBorrower(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowerRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Peminjam berhasil dihapus!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Peminjam tidak bisa dihapus karena masih terkait dengan transaksi inventory.");
        }
        return "redirect:/borrower";
    }

    @PostMapping("/query")
    public String queryBorrower(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Borrower> borrowerPage;
        if (keyword != null && !keyword.isEmpty()) {
            borrowerPage = borrowerRepository.findByEmployeeNoContainingIgnoreCaseOrNamaContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            borrowerPage = borrowerRepository.findAll(pageable);
        }

        User loginUser = (User) session.getAttribute("user");
        String factory = loginUser != null ? loginUser.getFactory() : null;
        String username = loginUser != null ? loginUser.getUsername() : null;
        String role = loginUser != null ? loginUser.getRole() : null;
        boolean canCrudEmployee = loginUser != null && "CRUD_EMPLOYEE".equals(loginUser.getAuthority());

        model.addAttribute("borrowers", borrowerPage.getContent());
        model.addAttribute("borrowerPage", borrowerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", borrowerPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("keyword", keyword);

        model.addAttribute("username", username);
        model.addAttribute("factory", factory);
        model.addAttribute("role", role);
        model.addAttribute("canCrudEmployee", canCrudEmployee);
        model.addAttribute("ip", request.getRemoteAddr());

        return "borrowerList";
    }
}