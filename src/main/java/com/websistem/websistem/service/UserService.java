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
}