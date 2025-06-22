package com.websistem.websistem.controller;

import com.websistem.websistem.model.AuditLog;
import com.websistem.websistem.model.EmployeeFiwa;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.AuditLogRepository;
import com.websistem.websistem.repository.EmployeeFiwaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeFiwaRepository employeeFiwaRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/dataEmployee")
public String dataEmployee(Model model, HttpSession session, HttpServletRequest request) {
    User loginUser = (User) session.getAttribute("user");
    String factory = loginUser != null ? loginUser.getFactory() : null;
    List<EmployeeFiwa> employeeList = employeeFiwaRepository.findAllByFactoryAndStatus(factory, "AKTIF");
    model.addAttribute("employeeList", new ArrayList<>()); // kosong saat awal
    

    if (loginUser != null) {
        model.addAttribute("username", loginUser.getUsername());
        model.addAttribute("factory", loginUser.getFactory());
        model.addAttribute("role", loginUser.getRole());

        // Enable CRUD jika authority TIDAK null dan TIDAK kosong
        boolean canCrudEmployee = "CRUD_EMPLOYEE".equals(loginUser.getAuthority());
        model.addAttribute("canCrudEmployee", canCrudEmployee);
    }
    model.addAttribute("employeeFiwa", new EmployeeFiwa());
    model.addAttribute("ip", request.getRemoteAddr());

    System.out.println("Jumlah data employee: " + employeeList.size()); // Debug
    return "dataEmployee";
}

    @PostMapping("/upload-employee")
    @Transactional
    public String uploadEmployee(MultipartFile file, Model model, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<EmployeeFiwa> employeesList = new ArrayList<>();
        List<String> duplikatList = new ArrayList<>();
        User user = (User) session.getAttribute("user");
        String factory = user != null ? user.getFactory() : null;
        // Tambahkan set untuk deteksi duplikat di file
        Set<String> uniqueKeySet = new HashSet<>();
        try (InputStream is = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            boolean header = true;
        while (rows.hasNext()) {
        Row row = rows.next();
        if (header) {
            header = false;
            continue;
        }
        if (row == null || row.getCell(0) == null || row.getCell(0).toString().trim().isEmpty()) {
            continue;
        }
        // Tambahan: skip baris jika kolom pertama adalah "Employee No."
        if ("Employee No.".equalsIgnoreCase(getCellValue(row, 0))) {
            continue;
        }
        String employeeNo = getCellValue(row, 0);
        String startDate = getCellValue(row, 28); // index 28 untuk startDate

                // Cek duplikat di file (bukan hanya di database)
                String uniqueKey = employeeNo + "_" + startDate;
                if (uniqueKeySet.contains(uniqueKey)) {
                    continue; // skip baris duplikat di file
                }
                uniqueKeySet.add(uniqueKey);

                // Cek apakah data sudah ada di database
                boolean exists = employeeFiwaRepository.existsByEmployeeNoAndStartDate(employeeNo, startDate);
                if (exists) {
                    duplikatList.add(employeeNo + " (" + startDate + ")");
                    continue;
                }

                EmployeeFiwa employee = new EmployeeFiwa();
                employee.setEmployeeNo(employeeNo);
                employee.setName(getCellValue(row, 1));
                employee.setGender(getCellValue(row, 2));
                employee.setDeptCode(getCellValue(row, 11));
                employee.setGroupName(getCellValue(row, 12));
                employee.setStartDate(startDate);
                employee.setFactory(factory); // Set factory sesuai user login
                employeesList.add(employee);

                employee.setStatus("AKTIF");
                employee.setResignDate(null);

            }

            if (!employeesList.isEmpty()) {
                employeeFiwaRepository.saveAll(employeesList);
                employeeFiwaRepository.flush();
                redirectAttributes.addFlashAttribute("success", "Data berhasil diupload: " + employeesList.size());
            }
            if (!duplikatList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Data duplikat tidak diupload: " + String.join(", ", duplikatList));
            }
            if (employeesList.isEmpty() && duplikatList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tidak ada data yang disimpan (list kosong).");
            }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Gagal upload file: " + e.getMessage());
            }
        // Ambil data terbaru sesuai factory user login
        List<EmployeeFiwa> employeeList = employeeFiwaRepository.findAllByFactory(factory);
        model.addAttribute("employeeList", employeeList);
        if (user != null) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("factory", user.getFactory());
            boolean canCrudEmployee = "CRUD_EMPLOYEE".equals(user.getAuthority());
            model.addAttribute("canCrudEmployee", canCrudEmployee);
        }

        // Audit log upload karyawan aktif
        AuditLog log = new AuditLog();
        log.setUsername(user != null ? user.getUsername() : "unknown");
        log.setIp(request.getRemoteAddr());
        log.setAction("UPLOAD_EMPLOYEE");
        log.setEntityName("EmployeeFiwa");
        log.setEntityId(factory);
        log.setDescription("Upload data karyawan aktif: " + employeesList.size() + " data, duplikat: " + duplikatList.size());
        log.setTimestamp(java.time.LocalDateTime.now());
        auditLogRepository.save(log);

        model.addAttribute("ip", request.getRemoteAddr());
        return "redirect:/dataEmployee";
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate().toString();
            }
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.toString().trim();
    }

    @PostMapping("/add-employee")
    public String addEmployee(@ModelAttribute EmployeeFiwa employeeFiwa, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User loginUser = (User) session.getAttribute("user");
        if (loginUser != null) {
            employeeFiwa.setFactory(loginUser.getFactory()); // Set factory sesuai user login
            // Cek duplikasi Employee No di factory yang sama
            boolean exists = employeeFiwaRepository.existsByEmployeeNoAndFactory(employeeFiwa.getEmployeeNo(), loginUser.getFactory());
            if (exists) {
                redirectAttributes.addFlashAttribute("error", "Employee No sudah terdaftar di factory Anda!");
                return "redirect:/dataEmployee";
            }
        }

        employeeFiwa.setStatus("AKTIF");
        employeeFiwa.setResignDate(null);

        employeeFiwaRepository.save(employeeFiwa);
        redirectAttributes.addFlashAttribute("success", "Karyawan berhasil ditambah!");

        // Audit log
        AuditLog log = new AuditLog();
        log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
        log.setIp(request.getRemoteAddr());
        log.setAction("ADD_EMPLOYEE");
        log.setEntityName("EmployeeFiwa");
        log.setEntityId(employeeFiwa.getEmployeeNo());
        log.setDescription("Tambah karyawan: " + employeeFiwa.getName());
        log.setTimestamp(java.time.LocalDateTime.now());
        auditLogRepository.save(log);

        return "redirect:/dataEmployee";
    }

    @PostMapping("/edit-employee")
public String editEmployee(@ModelAttribute EmployeeFiwa employeeFiwa, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    EmployeeFiwa existing = employeeFiwaRepository.findById(employeeFiwa.getId()).orElse(null);
    if (existing != null) {
        existing.setEmployeeNo(employeeFiwa.getEmployeeNo());
        existing.setName(employeeFiwa.getName());
        existing.setGender(employeeFiwa.getGender());
        existing.setDeptCode(employeeFiwa.getDeptCode());
        existing.setGroupName(employeeFiwa.getGroupName());
        existing.setStartDate(employeeFiwa.getStartDate());

        User loginUser = (User) session.getAttribute("user");
        if (loginUser != null) {
            existing.setFactory(loginUser.getFactory());
        }

        employeeFiwaRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Karyawan berhasil diupdate!");

        // Audit log
        AuditLog log = new AuditLog();
        log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
        log.setIp(request.getRemoteAddr());
        log.setAction("EDIT_EMPLOYEE");
        log.setEntityName("EmployeeFiwa");
        log.setEntityId(existing.getEmployeeNo());
        log.setDescription("Edit karyawan: " + existing.getName());
        log.setTimestamp(java.time.LocalDateTime.now());
        auditLogRepository.save(log);
    } else {
        redirectAttributes.addFlashAttribute("error", "Data karyawan tidak ditemukan!");
    }
    return "redirect:/dataEmployee";
}

   @PostMapping("/delete-employee/{id}")
public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
    EmployeeFiwa emp = employeeFiwaRepository.findById(id).orElse(null);
    employeeFiwaRepository.deleteById(id);
    redirectAttributes.addFlashAttribute("success", "Karyawan berhasil dihapus!");

    // Audit log
    User loginUser = (User) session.getAttribute("user");
    AuditLog log = new AuditLog();
    log.setUsername(loginUser != null ? loginUser.getUsername() : "unknown");
    log.setIp(request.getRemoteAddr());
    log.setAction("DELETE_EMPLOYEE");
    log.setEntityName("EmployeeFiwa");
    log.setEntityId(emp != null ? emp.getEmployeeNo() : String.valueOf(id));
    log.setDescription("Hapus karyawan: " + (emp != null ? emp.getName() : "-"));
    log.setTimestamp(java.time.LocalDateTime.now());
    auditLogRepository.save(log);

    return "redirect:/dataEmployee";
}

    @PostMapping("/upload-employee-resign")
    @Transactional
    public String uploadEmployeeResign(MultipartFile file, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        List<String> notFoundList = new ArrayList<>();
        List<String> updatedList = new ArrayList<>();
        User user = (User) session.getAttribute("user");
        String factory = user != null ? user.getFactory() : null;

        try (InputStream is = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            boolean header = true;
            while (rows.hasNext()) {
                Row row = rows.next();
                if (header) {
                    header = false;
                    continue;
                }
                if (row == null || row.getCell(0) == null || row.getCell(0).toString().trim().isEmpty()) {
                    continue;
                }
                String employeeNo = getCellValue(row, 4);
                String resignDate = getCellValue(row, 11); // Asumsi kolom ke-1 adalah resign_date

                // Cari karyawan berdasarkan employeeNo dan factory
                EmployeeFiwa emp = employeeFiwaRepository.findByEmployeeNoAndFactory(employeeNo, factory);
                if (emp != null) {
                    emp.setStatus("RESIGN");
                    emp.setResignDate(resignDate);
                    employeeFiwaRepository.save(emp);
                    updatedList.add(employeeNo);
                } else {
                    notFoundList.add(employeeNo);
                }
            }

            if (!updatedList.isEmpty()) {
                redirectAttributes.addFlashAttribute("success", "Data resign berhasil diupdate: " + updatedList.size());
            }
            if (!notFoundList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "NIK tidak ditemukan: " + String.join(", ", notFoundList));
            }
            if (updatedList.isEmpty() && notFoundList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tidak ada data yang diproses.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal upload file: " + e.getMessage());
        }

        // Audit log upload resign
        AuditLog log = new AuditLog();
        log.setUsername(user != null ? user.getUsername() : "unknown");
        log.setIp(request.getRemoteAddr()); // <-- simpan IP user
        log.setAction("UPLOAD_EMPLOYEE_RESIGN");
        log.setEntityName("EmployeeFiwa");
        log.setEntityId(factory);
        log.setDescription("Upload data resign: update " + updatedList.size() + ", not found: " + notFoundList.size());
        log.setTimestamp(java.time.LocalDateTime.now());
        auditLogRepository.save(log);

        return "redirect:/dataEmployee";
    }

    @PostMapping("/queryEmployee")
    public String queryEmployee(
        @RequestParam(required = false) String employeeNo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size, // <-- ubah jadi 15
        Model model, HttpSession session, HttpServletRequest request) {

        User loginUser = (User) session.getAttribute("user");
        String factory = loginUser != null ? loginUser.getFactory() : null;
        Page<EmployeeFiwa> employeePage;

        Pageable pageable = PageRequest.of(page, size);

        if (employeeNo != null && !employeeNo.isEmpty()) {
            employeePage = employeeFiwaRepository.findByFactoryAndEmployeeNo(factory, employeeNo, pageable);
        } else {
            employeePage = employeeFiwaRepository.findAllByFactoryAndStatus(factory, "AKTIF", pageable);
        }

        model.addAttribute("employeePage", employeePage != null ? employeePage : Page.empty());
        model.addAttribute("employeeList", employeePage != null ? employeePage.getContent() : new ArrayList<>());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage != null ? employeePage.getTotalPages() : 1);
        model.addAttribute("pageSize", size);

        if (loginUser != null) {
            model.addAttribute("username", loginUser.getUsername());
            model.addAttribute("factory", loginUser.getFactory());
            model.addAttribute("role", loginUser.getRole());
            boolean canCrudEmployee = "CRUD_EMPLOYEE".equals(loginUser.getAuthority());
            model.addAttribute("canCrudEmployee", canCrudEmployee);
        }
        model.addAttribute("employeeFiwa", new EmployeeFiwa());
        model.addAttribute("ip", request.getRemoteAddr());

        return "dataEmployee";
    }

    @GetMapping("/export-employee-excel")
    public void exportEmployeeExcel(HttpServletResponse response, HttpSession session) throws Exception {
        User loginUser = (User) session.getAttribute("user");
        String factory = loginUser != null ? loginUser.getFactory() : null;
        List<EmployeeFiwa> allEmployees = employeeFiwaRepository.findAllByFactoryAndStatus(factory, "AKTIF");

        // Buat workbook Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Karyawan");

        // Style border
        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        
        // Header
        Row header = sheet.createRow(0);
        String[] headers = {"Employee No", "Nama", "Gender", "Dept Code", "Group Name", "Start Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(borderStyle);
        }

        // Data
        int rowIdx = 1;
        for (EmployeeFiwa emp : allEmployees) {
            Row row = sheet.createRow(rowIdx++);
            Cell cell0 = row.createCell(0); cell0.setCellValue(emp.getEmployeeNo()); cell0.setCellStyle(borderStyle);
            Cell cell1 = row.createCell(1); cell1.setCellValue(emp.getName()); cell1.setCellStyle(borderStyle);
            Cell cell2 = row.createCell(2); cell2.setCellValue(emp.getGender()); cell2.setCellStyle(borderStyle);
            Cell cell3 = row.createCell(3); cell3.setCellValue(emp.getDeptCode()); cell3.setCellStyle(borderStyle);
            Cell cell4 = row.createCell(4); cell4.setCellValue(emp.getGroupName()); cell4.setCellStyle(borderStyle);
            Cell cell5 = row.createCell(5); cell5.setCellValue(emp.getStartDate()); cell5.setCellStyle(borderStyle);
        }

        // Autosize columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        response.setHeader("Content-Disposition", "attachment; filename=DataKaryawan_" + timestamp + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}