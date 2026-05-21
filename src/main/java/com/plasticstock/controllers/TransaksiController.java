package com.plasticstock.controllers;

import com.plasticstock.database.DatabaseConnection;
import com.plasticstock.models.Barang;
import com.plasticstock.models.Transaksi;
import com.plasticstock.utils.AlertHelper;
import com.plasticstock.utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import com.plasticstock.utils.FormatUtil;

public class TransaksiController implements Initializable {

    // Form
    @FXML private Label          lblNoTransaksi;
    @FXML private ComboBox<Barang> cmbBarang;
    @FXML private ComboBox<String> cmbJenis;
    @FXML private TextField      txtJumlah;
    @FXML private TextField      txtHargaSatuan;
    @FXML private Label          lblTotal;
    @FXML private TextArea       txtKeterangan;

    // Tabel
    @FXML private TableView<Transaksi>             tblTransaksi;
    @FXML private TableColumn<Transaksi, String>   colNo;
    @FXML private TableColumn<Transaksi, String>   colJenis;
    @FXML private TableColumn<Transaksi, String>   colBarang;
    @FXML private TableColumn<Transaksi, Integer>  colJumlah;
    @FXML private TableColumn<Transaksi, Double>   colTotal;
    @FXML private TableColumn<Transaksi, String>   colTanggal;

    private ObservableList<Transaksi> transaksiList = FXCollections.observableArrayList();



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupForm();
        loadBarang();
        loadTransaksi();

        // Hitung total otomatis
        txtJumlah.textProperty().addListener((obs, o, n) -> hitungTotal());
        txtHargaSatuan.textProperty().addListener((obs, o, n) -> hitungTotal());
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleSimpan() {
        if (!validateForm()) return;

        Barang barang = cmbBarang.getValue();
        String jenis  = cmbJenis.getValue();
        int    jumlah = Integer.parseInt(txtJumlah.getText().trim());

        // Cek stok jika keluar
        if ("keluar".equals(jenis) && barang.getStok() < jumlah) {
            AlertHelper.showWarning("Stok Tidak Cukup",
                "Stok tersedia: " + barang.getStok() + " " + barang.getSatuan());
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Insert transaksi
            String sqlT = "INSERT INTO transaksi (no_transaksi, jenis_transaksi, barang_id, jumlah, "
                        + "harga_satuan, total_harga, keterangan, tanggal) VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlT)) {
                double harga = Double.parseDouble(txtHargaSatuan.getText().trim());
                ps.setString(1, lblNoTransaksi.getText());
                ps.setString(2, jenis);
                ps.setInt(3, barang.getId());
                ps.setInt(4, jumlah);
                ps.setDouble(5, harga);
                ps.setDouble(6, harga * jumlah);
                ps.setString(7, txtKeterangan.getText());
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            // Update stok
            String delta = "masuk".equals(jenis) ? "stok + ?" : "stok - ?";
            String sqlS  = "UPDATE barang SET stok = " + delta + " WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlS)) {
                ps.setInt(1, jumlah);
                ps.setInt(2, barang.getId());
                ps.executeUpdate();
            }

            conn.commit();
            AlertHelper.showInfo("Sukses", "Transaksi berhasil disimpan.");

        } catch (SQLException e) {
            AlertHelper.showError("Error", e.getMessage());
        }

        clearForm();
        loadTransaksi();
        generateNoTransaksi();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void setupTable() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("noTransaksi"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenisTransaksi"));
        colBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));

        // FORMAT RUPIAH
        colTotal.setCellFactory(column -> new TableCell<Transaksi, Double>() {

            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(FormatUtil.rupiah(value));
                }
            }
        });

        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        tblTransaksi.setItems(transaksiList);
    }

    private void setupForm() {
        cmbJenis.setItems(FXCollections.observableArrayList("masuk", "keluar"));
        cmbJenis.getSelectionModel().selectFirst();
        generateNoTransaksi();
    }

    private void loadBarang() {
        ObservableList<Barang> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM barang ORDER BY nama_barang";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Barang(
                    rs.getInt("id"), rs.getString("kode_barang"),
                    rs.getString("nama_barang"), rs.getString("kategori"),
                    rs.getInt("stok"), rs.getDouble("harga_beli"),
                    rs.getDouble("harga_jual"), rs.getString("satuan")
                ));
            }
        } catch (SQLException e) {
            // Barang belum ada di DB
        }
        cmbBarang.setItems(list);
    }

    private void loadTransaksi() {
        transaksiList.clear();
        String sql = "SELECT t.*, b.nama_barang FROM transaksi t "
                   + "JOIN barang b ON t.barang_id = b.id "
                   + "ORDER BY t.tanggal DESC LIMIT 100";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            while (rs.next()) {
                Transaksi t = new Transaksi(
                    rs.getInt("id"), rs.getString("no_transaksi"),
                    rs.getString("jenis_transaksi"), rs.getInt("barang_id"),
                    rs.getString("nama_barang"), rs.getInt("jumlah"),
                    rs.getDouble("harga_satuan"), rs.getDouble("total_harga"),
                    rs.getString("keterangan"),
                    rs.getTimestamp("tanggal").toLocalDateTime()
                );
                transaksiList.add(t);
            }
        } catch (SQLException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    private void generateNoTransaksi() {
        String prefix = "TRX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String sql    = "SELECT COUNT(*) + 1 AS next FROM transaksi WHERE DATE(tanggal) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                lblNoTransaksi.setText(prefix + String.format("%04d", rs.getInt("next")));
            }
        } catch (Exception e) {
            lblNoTransaksi.setText(prefix + "0001");
        }
    }

    private void hitungTotal() {
        try {
            double harga  = Double.parseDouble(txtHargaSatuan.getText().trim());
            int    jumlah = Integer.parseInt(txtJumlah.getText().trim());
            lblTotal.setText(FormatUtil.rupiah(harga * jumlah));
        } catch (NumberFormatException e) {
            lblTotal.setText("Rp 0");
        }
    }

    private boolean validateForm() {
        if (cmbBarang.getValue() == null) {
            AlertHelper.showWarning("Validasi", "Pilih barang terlebih dahulu!"); return false;
        }
        if (!Validator.isValidInteger(txtJumlah.getText())) {
            AlertHelper.showWarning("Validasi", "Jumlah harus berupa angka!"); return false;
        }
        if (!Validator.isValidDouble(txtHargaSatuan.getText())) {
            AlertHelper.showWarning("Validasi", "Harga satuan harus berupa angka!"); return false;
        }
        return true;
    }

    private void clearForm() {
        cmbBarang.getSelectionModel().clearSelection();
        cmbJenis.getSelectionModel().selectFirst();
        txtJumlah.clear();
        txtHargaSatuan.clear();
        txtKeterangan.clear();
        lblTotal.setText("Rp 0");
    }
}
