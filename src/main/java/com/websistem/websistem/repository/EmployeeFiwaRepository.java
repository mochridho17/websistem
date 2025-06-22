package com.websistem.websistem.repository;

import com.websistem.websistem.model.EmployeeFiwa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Tambahkan baris ini
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeFiwaRepository extends JpaRepository<EmployeeFiwa, Long> {
    boolean existsByEmployeeNoAndStartDate(String employeeNo, String startDate);
    boolean existsByEmployeeNoAndFactory(String employeeNo, String factory);
    List<EmployeeFiwa> findAllByFactory(String factory);
    long countByFactory(String factory);
    EmployeeFiwa findByEmployeeNoAndFactory(String employeeNo, String factory);
    List<EmployeeFiwa> findAllByFactoryAndStatus(String factory, String status);
    List<EmployeeFiwa> findByFactoryAndEmployeeNo(String factory, String employeeNo);
    Page<EmployeeFiwa> findAllByFactoryAndStatus(String factory, String status, Pageable pageable);
    Page<EmployeeFiwa> findByFactoryAndEmployeeNo(String factory, String employeeNo, Pageable pageable);
}