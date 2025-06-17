package com.websistem.websistem.controller;

import com.websistem.websistem.model.EmployeeFiwa;
import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.EmployeeFiwaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/dataEmployee")
public String dataEmployee(Model model, HttpSession session, HttpServletRequest request) {
    User loginUser = (User) session.getAttribute("user");
    String factory = loginUser != null ? loginUser.getFactory() : null;
    List<EmployeeFiwa> employeeList = employeeFiwaRepository.findAllByFactory(factory);
    model.addAttribute("employeeList", employeeList);

    if (loginUser != null) {
        model.addAttribute("username", loginUser.getUsername());
        model.addAttribute("factory", loginUser.getFactory());
        model.addAttribute("role", loginUser.getRole());
    }
    model.addAttribute("employeeFiwa", new EmployeeFiwa());
    model.addAttribute("ip", request.getRemoteAddr());

    System.out.println("Jumlah data employee: " + employeeList.size()); // Debug
    return "dataEmployee";
}

    @PostMapping("/upload-employee")
@Transactional
public String uploadEmployee(MultipartFile file, Model model, HttpSession session, HttpServletRequest request) {
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
        }

        if (!employeesList.isEmpty()) {
            employeeFiwaRepository.saveAll(employeesList);
            employeeFiwaRepository.flush();
            model.addAttribute("success", "Data berhasil diupload: " + employeesList.size());
        }
        if (!duplikatList.isEmpty()) {
            model.addAttribute("error", "Data duplikat tidak diupload: " + String.join(", ", duplikatList));
        }
        if (employeesList.isEmpty() && duplikatList.isEmpty()) {
            model.addAttribute("error", "Tidak ada data yang disimpan (list kosong).");
        }
    } catch (Exception e) {
        model.addAttribute("error", "Gagal upload file: " + e.getMessage());
    }
    // Ambil data terbaru sesuai factory user login
    List<EmployeeFiwa> employeeList = employeeFiwaRepository.findAllByFactory(factory);
    model.addAttribute("employeeList", employeeList);
    if (user != null) {
        model.addAttribute("username", user.getUsername());
        model.addAttribute("factory", user.getFactory());
    }
    model.addAttribute("ip", request.getRemoteAddr());
    return "dataEmployee";
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
    public String addEmployee(@ModelAttribute EmployeeFiwa employeeFiwa, RedirectAttributes redirectAttributes) {
        employeeFiwaRepository.save(employeeFiwa);
        redirectAttributes.addFlashAttribute("success", "Karyawan berhasil ditambah!");
        return "redirect:/dataEmployee";
    }

    @PostMapping("/edit-employee")
    public String editEmployee(@ModelAttribute EmployeeFiwa employeeFiwa, RedirectAttributes redirectAttributes) {
        employeeFiwaRepository.save(employeeFiwa);
        redirectAttributes.addFlashAttribute("success", "Karyawan berhasil diupdate!");
        return "redirect:/dataEmployee";
    }

    @PostMapping("/delete-employee/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeFiwaRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Karyawan berhasil dihapus!");
        return "redirect:/dataEmployee";
    }
}