package com.websistem.websistem.repository;

import com.websistem.websistem.model.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsernameAndPasswordAndFactory(String username, String password, String factory);
    List<User> findByUsername(String username);
    List<User> findByUsernameAndFactory(String username, String factory); // Tambahkan ini
    List<User> findByUsernameAndRole(String username, String role);
    List<User> findByUsernameAndPasswordAndRole(String username, String password, String role);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<User> findAll(Pageable pageable);
    Page<User> findByUsernameNot(String username, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCaseAndUsernameNot(String username, String excluded, Pageable pageable);
}