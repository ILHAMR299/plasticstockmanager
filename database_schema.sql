CREATE DATABASE IF NOT EXISTS plastic_stock_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE plastic_stock_db;

CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       ENUM('admin', 'staff') NOT NULL DEFAULT 'staff',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password) VALUES
    ('admin_toko', 'hash_password_1');

CREATE TABLE IF NOT EXISTS barang (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    kode_barang  VARCHAR(20)     NOT NULL UNIQUE,
    nama_barang  VARCHAR(100)    NOT NULL,
    kategori     VARCHAR(50),
    stok         INT             NOT NULL DEFAULT 0,
    harga_beli   DECIMAL(15, 2)  NOT NULL DEFAULT 0,
    harga_jual   DECIMAL(15, 2)  NOT NULL DEFAULT 0,
    satuan       VARCHAR(20)     NOT NULL DEFAULT 'pcs',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO barang (kode_barang, nama_barang, kategori, stok, harga_beli, harga_jual, satuan) VALUES
    ('PLS001', 'Ember 20L',         'Wadah',   50,  25000, 35000, 'pcs'),
    ('PLS002', 'Botol Galon 19L',   'Botol',   30,  45000, 65000, 'pcs'),
    ('PLS003', 'Kantong Plastik 1K', 'Kantong', 200,  5000,  8000, 'kg'),
    ('PLS004', 'Kursi Plastik',     'Furnitur', 15,  55000, 85000, 'pcs'),
    ('PLS005', 'Tabungan Plastik',   'Tabungann',     8,  35000, 55000, 'batang');

CREATE TABLE IF NOT EXISTS transaksi (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    no_transaksi     VARCHAR(30)    NOT NULL UNIQUE,
    jenis_transaksi  ENUM('masuk', 'keluar') NOT NULL,
    barang_id        INT            NOT NULL,
    jumlah           INT            NOT NULL,
    harga_satuan     DECIMAL(15, 2) NOT NULL,
    total_harga      DECIMAL(15, 2) NOT NULL,
    keterangan       TEXT,
    tanggal          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (barang_id) REFERENCES barang(id) ON DELETE RESTRICT
);

CREATE INDEX idx_transaksi_tanggal  ON transaksi (tanggal);
CREATE INDEX idx_transaksi_barang   ON transaksi (barang_id);
CREATE INDEX idx_barang_kode        ON barang    (kode_barang);
