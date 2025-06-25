package com.websistem.websistem.repository;

import com.websistem.websistem.model.Borrower;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    List<Borrower> findByNamaContainingIgnoreCase(String nama);
    List<Borrower> findByEmployeeNoContainingIgnoreCaseOrNamaContainingIgnoreCase(String employeeNo, String nama);
    Page<Borrower> findByEmployeeNoContainingIgnoreCaseOrNamaContainingIgnoreCase(String employeeNo, String nama, Pageable pageable);
}