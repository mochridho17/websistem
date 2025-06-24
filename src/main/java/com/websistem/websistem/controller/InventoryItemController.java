package com.websistem.websistem.controller;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.model.InventoryItem;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AuditLogRepository;
import com.websistem.websistem.repository.InventoryItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/inventory-item")
public class InventoryItemController {

    @Autowired
    private InventoryItemRepository itemRepository;
    private AuditLogRepository auditLogRepository;
    

    InventoryItemController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String itemList(Model model, HttpSession session, HttpServletRequest request) {
        User loginUser = (User) session.getAttribute("user");
        String factory = loginUser != null ? loginUser.getFactory() : null;
        String username = loginUser != null ? loginUser.getUsername() : null;
        String role = loginUser != null ? loginUser.getRole() : null;
        boolean canCrudInventory = loginUser != null && "ADMIN".equalsIgnoreCase(loginUser.getRole());

        if (loginUser != null) {
                model.addAttribute("username", loginUser.getUsername());
                model.addAttribute("factory", loginUser.getFactory());
                model.addAttribute("role", loginUser.getRole());

                boolean canCrudEmployee = "CRUD_EMPLOYEE".equals(loginUser.getAuthority());
                model.addAttribute("canCrudEmployee", canCrudEmployee);
            }

        // Jangan ambil data apapun di sini!
        model.addAttribute("items", null); // atau Collections.emptyList()
        model.addAttribute("itemPage", null);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        model.addAttribute("pageSize", 10);

        model.addAttribute("factory", factory);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("canCrudInventory", canCrudInventory);
        model.addAttribute("ip", request.getRemoteAddr());

        return "inventoryItemList";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute InventoryItem item, RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
        if (itemRepository.existsByKodeBarang(item.getKodeBarang())) {
            redirectAttributes.addFlashAttribute("error", "Kode barang sudah terdaftar!");
            return "redirect:/inventory-item";
        }
        itemRepository.save(item);
        logAudit(session, request, "CREATE", "InventoryItem", item.getKodeBarang(), "Tambah item: " + item.getNamaBarang());
        redirectAttributes.addFlashAttribute("success", "Data berhasil ditambahkan!");
        return "redirect:/inventory-item";
    }

    @PostMapping("/edit")
    public String editItem(@ModelAttribute InventoryItem item, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        InventoryItem existing = itemRepository.findById(item.getId()).orElse(null);
        if (existing != null) {
            existing.setKodeBarang(item.getKodeBarang());
            existing.setNamaBarang(item.getNamaBarang());
            existing.setKategori(item.getKategori());
            existing.setMerk(item.getMerk());
            existing.setJumlah(item.getJumlah());
            existing.setLokasi(item.getLokasi());
            existing.setStatus(item.getStatus());
            existing.setKeterangan(item.getKeterangan());
            existing.setTanggalMasuk(item.getTanggalMasuk());
            existing.setTanggalUpdate(LocalDateTime.now());
            itemRepository.save(existing);
            logAudit(session, request, "UPDATE", "InventoryItem", existing.getKodeBarang(), "Edit item: " + existing.getNamaBarang());
            redirectAttributes.addFlashAttribute("success", "Data berhasil diubah!");
        }
        return "redirect:/inventory-item";
    }

    @PostMapping("/query")
    public String queryItems(
        @RequestParam(required = false) String kodeBarang,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model, HttpSession session, HttpServletRequest request) {

        User loginUser = (User) session.getAttribute("user");
        String factory = loginUser != null ? loginUser.getFactory() : null;
        String username = loginUser != null ? loginUser.getUsername() : null;
        String role = loginUser != null ? loginUser.getRole() : null;
        boolean canCrudInventory = loginUser != null && "ADMIN".equalsIgnoreCase(loginUser.getRole());

        // TAMBAHKAN INI:
        boolean canCrudEmployee = loginUser != null && "CRUD_EMPLOYEE".equals(loginUser.getAuthority());
        model.addAttribute("canCrudEmployee", canCrudEmployee);

        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryItem> itemPage;
        if (kodeBarang != null && !kodeBarang.isEmpty()) {
            itemPage = itemRepository.findByKodeBarangContainingIgnoreCase(kodeBarang, pageable);
        } else {
            itemPage = itemRepository.findAll(pageable);
        }

        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("itemPage", itemPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("pageSize", size);

        model.addAttribute("factory", factory);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("canCrudInventory", canCrudInventory);
        model.addAttribute("ip", request.getRemoteAddr());

        return "inventoryItemList";
    }

    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
        InventoryItem item = itemRepository.findById(id).orElse(null);
        if (item != null) {
            itemRepository.deleteById(id);
            logAudit(session, request, "DELETE", "InventoryItem", item.getKodeBarang(), "Hapus item: " + item.getNamaBarang());
            redirectAttributes.addFlashAttribute("success", "Data berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Data tidak ditemukan!");
        }
        return "redirect:/inventory-item";
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {
        List<InventoryItem> items = itemRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventory Items");

            // Header
            Row header = sheet.createRow(0);
            String[] columns = {"No", "Kode Barang", "Nama Barang", "Kategori", "Merk", "Jumlah", "Lokasi", "Status", "Keterangan", "Tanggal Masuk", "Update Terakhir"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data
            int rowIdx = 1;
            for (InventoryItem item : items) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(item.getKodeBarang());
                row.createCell(2).setCellValue(item.getNamaBarang());
                row.createCell(3).setCellValue(item.getKategori());
                row.createCell(4).setCellValue(item.getMerk());
                row.createCell(5).setCellValue(item.getJumlah() != null ? Double.parseDouble(item.getJumlah()) : 0.0);
                row.createCell(6).setCellValue(item.getLokasi());
                row.createCell(7).setCellValue(item.getStatus());
                row.createCell(8).setCellValue(item.getKeterangan());
                row.createCell(9).setCellValue(item.getTanggalMasuk() != null ? item.getTanggalMasuk().toString() : "");
                row.createCell(10).setCellValue(item.getTanggalUpdate() != null ? item.getTanggalUpdate().toString() : "");
            }

            // Autosize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=InventoryItems.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void logAudit(HttpSession session, HttpServletRequest request, String action, String entityName, String entityId, String description) {
    User loginUser = (User) session.getAttribute("user");
    AuditLog log = new AuditLog();
    log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
    log.setAction(action);
    log.setEntityName(entityName);
    log.setEntityId(entityId);
    log.setDescription(description);
    log.setTimestamp(LocalDateTime.now());
    log.setIp(request != null ? request.getRemoteAddr() : "-");
    auditLogRepository.save(log);
}
}