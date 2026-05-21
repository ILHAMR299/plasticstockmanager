package com.plasticstock.models;

import java.time.LocalDateTime;

public class Transaksi {

    private int           id;
    private String        noTransaksi;
    private String        jenisTransaksi; // "masuk" | "keluar"
    private int           barangId;
    private String        namaBarang;     // join dari tabel barang
    private int           jumlah;
    private double        hargaSatuan;
    private double        totalHarga;
    private String        keterangan;
    private LocalDateTime tanggal;

    public Transaksi() {}

    public Transaksi(int id, String noTransaksi, String jenisTransaksi,
                     int barangId, String namaBarang, int jumlah,
                     double hargaSatuan, double totalHarga,
                     String keterangan, LocalDateTime tanggal) {
        this.id              = id;
        this.noTransaksi     = noTransaksi;
        this.jenisTransaksi  = jenisTransaksi;
        this.barangId        = barangId;
        this.namaBarang      = namaBarang;
        this.jumlah          = jumlah;
        this.hargaSatuan     = hargaSatuan;
        this.totalHarga      = totalHarga;
        this.keterangan      = keterangan;
        this.tanggal         = tanggal;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int    getId()                    { return id; }
    public void   setId(int id)              { this.id = id; }

    public String getNoTransaksi()                       { return noTransaksi; }
    public void   setNoTransaksi(String noTransaksi)     { this.noTransaksi = noTransaksi; }

    public String getJenisTransaksi()                          { return jenisTransaksi; }
    public void   setJenisTransaksi(String jenisTransaksi)     { this.jenisTransaksi = jenisTransaksi; }

    public int  getBarangId()                  { return barangId; }
    public void setBarangId(int barangId)      { this.barangId = barangId; }

    public String getNamaBarang()                      { return namaBarang; }
    public void   setNamaBarang(String namaBarang)     { this.namaBarang = namaBarang; }

    public int  getJumlah()                { return jumlah; }
    public void setJumlah(int jumlah)      { this.jumlah = jumlah; }

    public double getHargaSatuan()                     { return hargaSatuan; }
    public void   setHargaSatuan(double hargaSatuan)   { this.hargaSatuan = hargaSatuan; }

    public double getTotalHarga()                    { return totalHarga; }
    public void   setTotalHarga(double totalHarga)   { this.totalHarga = totalHarga; }

    public String getKeterangan()                      { return keterangan; }
    public void   setKeterangan(String keterangan)     { this.keterangan = keterangan; }

    public LocalDateTime getTanggal()                      { return tanggal; }
    public void          setTanggal(LocalDateTime tanggal) { this.tanggal = tanggal; }
}
