package com.websistem.websistem.repository;

import com.websistem.websistem.model.EmployeeFiwa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Tambahkan baris ini

public interface EmployeeFiwaRepository extends JpaRepository<EmployeeFiwa, Long> {
    boolean existsByEmployeeNoAndStartDate(String employeeNo, String startDate);
    List<EmployeeFiwa> findAllByFactory(String factory);
    long countByFactory(String factory);
}