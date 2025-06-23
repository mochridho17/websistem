package com.websistem.websistem.model;

import jakarta.persistence.*;

@Entity
public class Borrower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employee_no;
    private String nama;
    private String departemen;
    private String kontak;

    
    public String getEmployee_no() {
        return employee_no;
    }
    public void setEmployee_no(String employee_no) {
        this.employee_no = employee_no;
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

    // getter & setter
}