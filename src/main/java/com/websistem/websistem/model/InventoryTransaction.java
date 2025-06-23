package com.websistem.websistem.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "barang_id")
    private InventoryItem barang;

    @ManyToOne
    @JoinColumn(name = "borrower_id")
    private Borrower borrower;

    private LocalDateTime tanggalTransaksi;
    private String jenisTransaksi; // Masuk, Keluar, Pinjam, Kembali
    private Integer jumlah;
    private String userPenerima;
    private String statusKeluar; // Pinjam, Keluar
    private LocalDate tanggalKembali;
    private String keterangan;
    private String admin;


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTanggalTransaksi() {
        return tanggalTransaksi;
    }
    public void setTanggalTransaksi(LocalDateTime tanggalTransaksi) {
        this.tanggalTransaksi = tanggalTransaksi;
    }
    public String getJenisTransaksi() {
        return jenisTransaksi;
    }
    public void setJenisTransaksi(String jenisTransaksi) {
        this.jenisTransaksi = jenisTransaksi;
    }
    public Integer getJumlah() {
        return jumlah;
    }
    public void setJumlah(Integer jumlah) {
        this.jumlah = jumlah;
    }
    public String getUserPenerima() {
        return userPenerima;
    }
    public void setUserPenerima(String userPenerima) {
        this.userPenerima = userPenerima;
    }
    public String getStatusKeluar() {
        return statusKeluar;
    }
    public void setStatusKeluar(String statusKeluar) {
        this.statusKeluar = statusKeluar;
    }
    public LocalDate getTanggalKembali() {
        return tanggalKembali;
    }
    public void setTanggalKembali(LocalDate tanggalKembali) {
        this.tanggalKembali = tanggalKembali;
    }
    public String getKeterangan() {
        return keterangan;
    }
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    public String getAdmin() {
        return admin;
    }
    public void setAdmin(String admin) {
        this.admin = admin;
    }

    // getter & setter
}
