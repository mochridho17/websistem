package com.websistem.websistem.model;

import jakarta.persistence.*;

@Entity
public class Borrower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employeeno")
    private String employeeNo;
    private String nama;
    private String departemen;
    private String kontak;
    private String faktory; // Tambahkan atribut faktory

    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmployeeNo() {
        return employeeNo;
    }
    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }
    public String getNama() {
        return nama;
    }
    public void setNama(String nama) {
        this.nama = nama;
    }
    public String getDepartemen() {
        return departemen;
    }
    public void setDepartemen(String departemen) {
        this.departemen = departemen;
    }
    public String getKontak() {
        return kontak;
    }
    public void setKontak(String kontak) {
        this.kontak = kontak;
    }

    public String getFaktory() {
        return faktory;
    }
    public void setFaktory(String faktory) {
        this.faktory = faktory;
    }
}