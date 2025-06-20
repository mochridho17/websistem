package com.websistem.websistem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // Nama tabel di database: users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Kolom id, auto increment, primary key

    @Column(nullable = false)
    private String username; // Kolom username, tidak boleh null, unik

    @Column(nullable = false)
    private String password; // Kolom password, tidak boleh null

    @Column(nullable = false)
    private String factory; // Kolom factory, tidak boleh null

    @Column(name = "created_person")
    private String createdPerson;

    @Column(nullable = false)
    private String role; // contoh: "ADMIN" atau "USER"

    private String authority;



    // --- Constructor ---
    public User() {
        
    }

    public User(String username, String password, String factory) {
        this.username = username;
        this.password = password;
        this.factory = factory;
        
    }

    

    // --- Getter & Setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getCreatedPerson() {
    return createdPerson;
    }
    public void setCreatedPerson(String createdPerson) {
        this.createdPerson = createdPerson;
    }

     public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}