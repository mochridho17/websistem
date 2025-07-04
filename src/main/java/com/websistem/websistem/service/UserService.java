package com.websistem.websistem.service;

import com.websistem.websistem.model.User;
import com.websistem.websistem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User login(String username, String password, String factory) {
        List<User> users = userRepository.findByUsernameAndPasswordAndFactory(username, password, factory);
        if (!users.isEmpty()) {
            return users.get(0); // Ambil user pertama
        } else {
            return null; // Tidak ditemukan
        }
        
    }

    public User findByUsernameAndFactory(String username, String factory) {
        List<User> users = userRepository.findByUsernameAndFactory(username, factory);
        if (!users.isEmpty()) {
            return users.get(0);
        } else {
            return null;
        }
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Cari user SUPER_DEV tanpa factory
    public User findByUsernameAndRole(String username, String role) {
        List<User> users = userRepository.findByUsernameAndRole(username, role);
        return users.isEmpty() ? null : users.get(0);
    }

    // Login SUPER_DEV tanpa factory
    public User login(String username, String password) {
        List<User> users = userRepository.findByUsernameAndPasswordAndRole(username, password, "SUPER_DEV");
        return users.isEmpty() ? null : users.get(0);
    }
    
}