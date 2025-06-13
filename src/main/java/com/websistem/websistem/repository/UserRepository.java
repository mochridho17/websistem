package com.websistem.websistem.repository;

import com.websistem.websistem.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsernameAndPasswordAndFactory(String username, String password, String factory);
    List<User> findByUsername(String username);
}