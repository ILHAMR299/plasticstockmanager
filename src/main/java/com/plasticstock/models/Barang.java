package com.plasticstock.models;

public class Barang {

    private int    id;
    private String kodeBarang;
    private String namaBarang;
    private String kategori;
    private int    stok;
    private double hargaBeli;
    private double hargaJual;
    private String satuan; // kg, pcs, karung, dll.

    public Barang() {}

    public Barang(int id, String kodeBarang, String namaBarang,
                  String kategori, int stok,
                  double hargaBeli, double hargaJual, String satuan) {
        this.id          = id;
        this.kodeBarang  = kodeBarang;
        this.namaBarang  = namaBarang;
        this.kategori    = kategori;
        this.stok        = stok;
        this.hargaBeli   = hargaBeli;
        this.hargaJual   = hargaJual;
        this.satuan      = satuan;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int    getId()                    { return id; }
    public void   setId(int id)              { this.id = id; }

    public String getKodeBarang()                      { return kodeBarang; }
    public void   setKodeBarang(String kodeBarang)     { this.kodeBarang = kodeBarang; }

    public String getNamaBarang()                      { return namaBarang; }
    public void   setNamaBarang(String namaBarang)     { this.namaBarang = namaBarang; }

    public String getKategori()                    { return kategori; }
    public void   setKategori(String kategori)     { this.kategori = kategori; }

    public int  getStok()               { return stok; }
    public void setStok(int stok)       { this.stok = stok; }

    public double getHargaBeli()                   { return hargaBeli; }
    public void   setHargaBeli(double hargaBeli)   { this.hargaBeli = hargaBeli; }

    public double getHargaJual()                   { return hargaJual; }
    public void   setHargaJual(double hargaJual)   { this.hargaJual = hargaJual; }

    public String getSatuan()                  { return satuan; }
    public void   setSatuan(String satuan)     { this.satuan = satuan; }

    @Override
    public String toString() {
        return namaBarang + " (" + kodeBarang + ")";
    }
}
