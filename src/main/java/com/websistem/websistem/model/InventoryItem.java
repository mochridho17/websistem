package com.websistem.websistem.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String kodeBarang;
    
    private String namaBarang;
    private String kategori;
    private String merk;
    private String jumlah;
    private String lokasi;
    private String status; // Tersedia, Dipinjam, Keluar
    private String keterangan;

    @Column(columnDefinition = "date")
    private LocalDate tanggalMasuk;

   @Column(columnDefinition = "timestamp")
   private LocalDateTime tanggalUpdate;

    // Getter & Setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }
    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public String getNamaBarang() {
        return namaBarang;
    }
    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getKategori() {
        return kategori;
    }
    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getMerk() {
        return merk;
    }
    public void setMerk(String merk) {
        this.merk = merk;
    }

    public String getJumlah() {
        return jumlah;
    }
    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getLokasi() {
        return lokasi;
    }
    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeterangan() {
        return keterangan;
    }
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public LocalDate getTanggalMasuk() {
        return tanggalMasuk;
    }
    public void setTanggalMasuk(LocalDate tanggalMasuk) {
        this.tanggalMasuk = tanggalMasuk;
    }

    public String getTanggalUpdateFormatted() {
    if (tanggalUpdate == null) return "";
    return tanggalUpdate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
}
    public void setTanggalUpdate(LocalDateTime tanggalUpdate) {
        this.tanggalUpdate = tanggalUpdate;
    }

    public LocalDateTime getTanggalUpdate() {
        return tanggalUpdate;
    }
}