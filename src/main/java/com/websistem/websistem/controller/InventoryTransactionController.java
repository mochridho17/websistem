package com.websistem.websistem.controller;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.model.Borrower;
import com.websistem.websistem.model.InventoryItem;
import com.websistem.websistem.model.InventoryTransaction;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AuditLogRepository;
import com.websistem.websistem.repository.InventoryTransactionRepository;

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

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inventory-transaction")
public class InventoryTransactionController {

    @Autowired
    private InventoryTransactionRepository transactionRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;


    @GetMapping("/inventoryTransactionList")
    public String inventoryTransactionList(Model model, HttpSession session, HttpServletRequest request) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser != null) {
            model.addAttribute("username", loginUser.getUsername());
            model.addAttribute("factory", loginUser.getFactory());
            model.addAttribute("role", loginUser.getRole());

            boolean canCrudEmployee = "CRUD_EMPLOYEE".equals(loginUser.getAuthority());
            model.addAttribute("canCrudEmployee", canCrudEmployee);
        }
        // TAMPILAN AWAL KOSONG
        model.addAttribute("transactions", null);
        model.addAttribute("currentPage", 0);
        model.addAttribute("pageSize", 10);
        model.addAttribute("totalPages", 0);
        model.addAttribute("itemPage", null);

        // ...tambahkan items, borrowers, user info seperti sebelumnya...
        List<InventoryItem> items = inventoryItemRepository.findAll();
        model.addAttribute("items", items);
        List<Borrower> borrowers = borrowerRepository.findAll();
        model.addAttribute("borrowers", borrowers);

        // ...user info...
        
        String factory = loginUser != null ? loginUser.getFactory() : "";
        String username = loginUser != null ? loginUser.getUsername() : "";
        String role = loginUser != null ? loginUser.getRole() : "";
        String ip = request.getRemoteAddr();
        boolean canCrudInventory = loginUser != null && "ADMIN".equalsIgnoreCase(loginUser.getRole());
        model.addAttribute("canCrudInventory", canCrudInventory);
        model.addAttribute("factory", factory);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("ip", ip);

        return "inventoryTransactionList";
    }

    @Autowired
    private com.websistem.websistem.repository.InventoryItemRepository inventoryItemRepository;
    @Autowired
    private com.websistem.websistem.repository.BorrowerRepository borrowerRepository;

    @PostMapping("/add")
    public String addTransaction(
            @ModelAttribute InventoryTransaction transaction,
            @RequestParam Long barangId,
            @RequestParam(required = false) Long borrowerId,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            HttpServletRequest request) {

        

        InventoryItem barang = inventoryItemRepository.findById(barangId).orElse(null);
        Borrower borrower = borrowerId != null ? borrowerRepository.findById(borrowerId).orElse(null) : null;
        transaction.setBarang(barang);
        transaction.setBorrower(borrower);

        // Update stok barang sesuai jenis transaksi
        if (barang != null && transaction.getJumlah() != null) {
            int jumlahTransaksi = transaction.getJumlah();
            String jenis = transaction.getJenisTransaksi();
            if ("Pinjam".equalsIgnoreCase(jenis) || "Keluar".equalsIgnoreCase(jenis)) {
                // Kurangi stok
                Integer stok = barang.getJumlah() != null ? Integer.parseInt(barang.getJumlah()) : 0;
                barang.setJumlah(String.valueOf(stok - jumlahTransaksi));
            } else if ("Kembali".equalsIgnoreCase(jenis)) {
                // Tambah stok
                Integer stok = barang.getJumlah() != null ? Integer.parseInt(barang.getJumlah()) : 0;
                barang.setJumlah(String.valueOf(stok + jumlahTransaksi));
            }

            // === Tambahkan logika status di sini ===
            int stokBaru = Integer.parseInt(barang.getJumlah());
            if ("Keluar".equalsIgnoreCase(jenis) && stokBaru <= 0) {
                barang.setStatus("Keluar");
            } else if (stokBaru > 0) {
                barang.setStatus("Tersedia");
            }
            // === END ===
            inventoryItemRepository.save(barang);
        }

        // Saat menambah transaksi
        User loginUser = (User) session.getAttribute("user");
        if (loginUser != null) {
            transaction.setUpdateBy(loginUser.getUsername());
        } else {
            transaction.setUpdateBy("unknown");
        }

        transactionRepository.save(transaction);
        logAudit(session, request, "CREATE", "InventoryTransaction", String.valueOf(transaction.getId()), "Tambah transaksi: " + transaction.getJenisTransaksi());
        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil ditambahkan!");
        return "redirect:/inventory-transaction/inventoryTransactionList";
    }

    @PostMapping("/edit")
    public String editTransaction(
            @ModelAttribute InventoryTransaction transaction,
            @RequestParam Long barangId,
            @RequestParam(required = false) Long borrowerId,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            HttpServletRequest request) {

        InventoryTransaction trx = transactionRepository.findById(transaction.getId()).orElse(null);
        if (trx == null) {
            redirectAttributes.addFlashAttribute("error", "Transaksi tidak ditemukan!");
            return "redirect:/inventory-transaction/inventoryTransactionList";
        }

        InventoryItem barang = inventoryItemRepository.findById(barangId).orElse(null);
        Borrower borrower = borrowerId != null ? borrowerRepository.findById(borrowerId).orElse(null) : null;

        // --- LOGIKA UPDATE STOK BARANG SAAT EDIT ---
        if (barang != null && transaction.getJumlah() != null) {
            String jenis = transaction.getJenisTransaksi();
            int jumlahLama = trx.getJumlah() != null ? trx.getJumlah() : 0;
            int jumlahBaru = transaction.getJumlah();
            String jenisLama = trx.getJenisTransaksi();
            String jenisBaru = transaction.getJenisTransaksi();

            int stok = barang.getJumlah() != null ? Integer.parseInt(barang.getJumlah()) : 0;

            // Kembalikan stok ke posisi sebelum transaksi lama
            if ("Pinjam".equalsIgnoreCase(jenisLama) || "Keluar".equalsIgnoreCase(jenisLama)) {
                stok += jumlahLama;
            } else if ("Kembali".equalsIgnoreCase(jenisLama)) {
                stok -= jumlahLama;
            }

            // Terapkan perubahan transaksi baru
            if ("Pinjam".equalsIgnoreCase(jenisBaru) || "Keluar".equalsIgnoreCase(jenisBaru)) {
                stok -= jumlahBaru;
            } else if ("Kembali".equalsIgnoreCase(jenisBaru)) {
                stok += jumlahBaru;
            }

            // === Tambahkan logika status di sini ===
            int stokBaru = Integer.parseInt(barang.getJumlah());
            if ("Keluar".equalsIgnoreCase(jenis) && stokBaru <= 0) {
                barang.setStatus("Keluar");
            } else if (stokBaru > 0) {
                barang.setStatus("Tersedia");
            }
            // === END ===

            barang.setJumlah(String.valueOf(stok));
            inventoryItemRepository.save(barang);
        }
        // --- END LOGIKA STOK ---

        trx.setBarang(barang);
        trx.setBorrower(borrower);
        trx.setJenisTransaksi(transaction.getJenisTransaksi());
        trx.setJumlah(transaction.getJumlah());
        trx.setTanggalTransaksi(transaction.getTanggalTransaksi());
        trx.setStatusKeluar(transaction.getStatusKeluar());
        trx.setTanggalKembali(transaction.getTanggalKembali());
        trx.setKeterangan(transaction.getKeterangan());

        // Update updateBy sesuai user login
        User loginUser = (User) session.getAttribute("user");
        trx.setUpdateBy(loginUser != null ? loginUser.getUsername() : "unknown");

        transactionRepository.save(trx);
        logAudit(session, request, "UPDATE", "InventoryTransaction", String.valueOf(trx.getId()), "Edit transaksi: " + trx.getJenisTransaksi());
        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil diubah!");
        return "redirect:/inventory-transaction/inventoryTransactionList";
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
        InventoryTransaction trx = transactionRepository.findById(id).orElse(null);
        if (trx != null) {
            transactionRepository.deleteById(id);
            logAudit(session, request, "DELETE", "InventoryTransaction", String.valueOf(id), "Hapus transaksi: " + trx.getJenisTransaksi());
            redirectAttributes.addFlashAttribute("success", "Transaksi berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Transaksi tidak ditemukan!");
        }
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

    @PostMapping("/query")
    public String queryTransactions(
            @RequestParam(required = false) String kodeBarang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, size);

        Page<InventoryTransaction> trxPage;
        if (kodeBarang != null && !kodeBarang.isEmpty()) {
            trxPage = transactionRepository.findByBarang_KodeBarangContainingIgnoreCase(kodeBarang, pageable);
        } else {
            trxPage = transactionRepository.findAll(pageable);
        }

        User loginUser = (User) session.getAttribute("user");
        if (loginUser != null) {
            model.addAttribute("username", loginUser.getUsername());
            model.addAttribute("factory", loginUser.getFactory());
            model.addAttribute("role", loginUser.getRole());

            boolean canCrudEmployee = "CRUD_EMPLOYEE".equals(loginUser.getAuthority());
            model.addAttribute("canCrudEmployee", canCrudEmployee);
        }

        model.addAttribute("transactions", trxPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", trxPage.getTotalPages());
        model.addAttribute("itemPage", trxPage);

        // ...tambahkan items, borrowers, user info seperti sebelumnya...
        List<InventoryItem> items = inventoryItemRepository.findAll();
        model.addAttribute("items", items);
        List<Borrower> borrowers = borrowerRepository.findAll();
        model.addAttribute("borrowers", borrowers);

        // ...user info...
        
        String factory = loginUser != null ? loginUser.getFactory() : "";
        String username = loginUser != null ? loginUser.getUsername() : "";
        String role = loginUser != null ? loginUser.getRole() : "";
        String ip = request.getRemoteAddr();
        boolean canCrudInventory = loginUser != null && "ADMIN".equalsIgnoreCase(loginUser.getRole());
        model.addAttribute("canCrudInventory", canCrudInventory);
        model.addAttribute("factory", factory);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("ip", ip);

        // Untuk menampilkan kembali filter di form
        model.addAttribute("param", Map.of("kodeBarang", kodeBarang));

        return "inventoryTransactionList";
    }
    
}