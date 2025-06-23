package com.websistem.websistem.controller;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.model.InventoryTransaction;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AuditLogRepository;
import com.websistem.websistem.repository.InventoryTransactionRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory-transaction")
public class InventoryTransactionController {

    @Autowired
    private InventoryTransactionRepository transactionRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/inventoryTransactionList")
    public String inventoryTransactionList(Model model, HttpSession session, HttpServletRequest request) {
        List<InventoryTransaction> transactions = transactionRepository.findAll();
        model.addAttribute("transactions", transactions);

        // Ambil user dari session
        User loginUser = (User) session.getAttribute("user");
        String factory = loginUser != null ? loginUser.getFactory() : "";
        String username = loginUser != null ? loginUser.getUsername() : "";
        String role = loginUser != null ? loginUser.getRole() : "";
        String ip = request.getRemoteAddr();

        // Tambahkan ini:
        boolean canCrudInventory = loginUser != null && "ADMIN".equalsIgnoreCase(loginUser.getRole());
        model.addAttribute("canCrudInventory", canCrudInventory);

        model.addAttribute("factory", factory);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("ip", ip);

        return "inventoryTransactionList";
    }


    @PostMapping("/add")
    public String addTransaction(@ModelAttribute InventoryTransaction transaction, RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
        User loginUser = (User) session.getAttribute("user");
    
        transactionRepository.save(transaction);
        logAudit(session, request, "CREATE", "InventoryTransaction", String.valueOf(transaction.getId()), "Tambah transaksi: " + transaction.getJenisTransaksi());
        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil ditambahkan!");
        return "redirect:/inventory-transaction/inventoryTransactionList";
    }

    // Tambahkan method logAudit seperti di InventoryItemController
    private void logAudit(HttpSession session, HttpServletRequest request, String action, String entityName, String entityId, String description) {
        User loginUser = (User) session.getAttribute("user");
        AuditLog log = new AuditLog();
        log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setTimestamp(java.time.LocalDateTime.now());
        log.setIp(request != null ? request.getRemoteAddr() : "-");
        auditLogRepository.save(log);
    }

    
}