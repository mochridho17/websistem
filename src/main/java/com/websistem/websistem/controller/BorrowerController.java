package com.websistem.websistem.controller;

import com.websistem.websistem.model.Borrower;
import com.websistem.websistem.repository.BorrowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/borrower")
public class BorrowerController {

    @Autowired
    private BorrowerRepository borrowerRepository;

    @GetMapping
    public String listBorrower(Model model) {
        
        model.addAttribute("borrowers", null);
        return "borrowerList"; // Buat template borrowerList.html
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
        borrowerRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Peminjam berhasil dihapus!");
        return "redirect:/borrower";
    }

    @PostMapping("/query")
    public String queryBorrower(@RequestParam(required = false) String nama, Model model) {
        List<Borrower> borrowers;
        if (nama != null && !nama.isEmpty()) {
            borrowers = borrowerRepository.findByNamaContainingIgnoreCase(nama);
        } else {
            borrowers = borrowerRepository.findAll();
        }
        model.addAttribute("borrowers", borrowers);
        model.addAttribute("nama", nama);
        return "borrowerList";
    }
}