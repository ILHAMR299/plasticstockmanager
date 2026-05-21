package com.plasticstock.controllers;

import com.plasticstock.database.DatabaseConnection;
import com.plasticstock.models.Barang;
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
import java.util.ResourceBundle;
import javafx.scene.control.TableCell;
import com.plasticstock.utils.FormatUtil;

public class BarangController implements Initializable {

    // Form fields
    @FXML private TextField txtKode;
    @FXML private TextField txtNama;
    @FXML private TextField txtKategori;
    @FXML private TextField txtStok;
    @FXML private TextField txtHargaBeli;
    @FXML private TextField txtHargaJual;
    @FXML private TextField txtSatuan;
    @FXML private TextField txtCari;

    // Table
    @FXML private TableView<Barang>            tblBarang;
    @FXML private TableColumn<Barang, String>  colKode;
    @FXML private TableColumn<Barang, String>  colNama;
    @FXML private TableColumn<Barang, String>  colKategori;
    @FXML private TableColumn<Barang, Integer> colStok;
    @FXML private TableColumn<Barang, Double>  colHargaBeli;
    @FXML private TableColumn<Barang, Double>  colHargaJual;
    @FXML private TableColumn<Barang, String>  colSatuan;


    private ObservableList<Barang> barangList = FXCollections.observableArrayList();
    private Barang selectedBarang = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadBarang();

        // Klik baris → isi form
        tblBarang.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) populateForm(newVal);
            }
        );

        // Pencarian real-time
        txtCari.textProperty().addListener((obs, old, val) -> filterBarang(val));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleSimpan() {
        if (!validateForm()) return;

        if (selectedBarang == null) {
            insertBarang();
        } else {
            updateBarang();
        }
        clearForm();
        loadBarang();
    }

    @FXML
    private void handleHapus() {
        if (selectedBarang == null) {
            AlertHelper.showWarning("Peringatan", "Pilih barang yang ingin dihapus!");
            return;
        }
        if (!AlertHelper.showConfirmation("Hapus", "Hapus barang " + selectedBarang.getNamaBarang() + "?")) return;

        String sql = "DELETE FROM barang WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedBarang.getId());
            ps.executeUpdate();
            AlertHelper.showInfo("Sukses", "Barang berhasil dihapus.");
        } catch (SQLException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
        clearForm();
        loadBarang();
    }

    @FXML
    private void handleBaru() {
        clearForm();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void setupTable() {

        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));

        colHargaBeli.setCellValueFactory(new PropertyValueFactory<>("hargaBeli"));
        colHargaJual.setCellValueFactory(new PropertyValueFactory<>("hargaJual"));

        //format rupiah beli
        colHargaBeli.setCellFactory(column ->
                new TableCell<Barang, Double>() {

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

        //format rupiah jual
        colHargaJual.setCellFactory(column ->
                new TableCell<Barang, Double>() {

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

        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));

        tblBarang.setItems(barangList);
    }
    private void loadBarang() {
        barangList.clear();
        String sql = "SELECT * FROM barang ORDER BY nama_barang";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                barangList.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    private void filterBarang(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tblBarang.setItems(barangList);
            return;
        }
        ObservableList<Barang> filtered = FXCollections.observableArrayList();
        for (Barang b : barangList) {
            if (b.getNamaBarang().toLowerCase().contains(keyword.toLowerCase())
                    || b.getKodeBarang().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(b);
            }
        }
        tblBarang.setItems(filtered);
    }

    private void insertBarang() {
        String sql = "INSERT INTO barang (kode_barang, nama_barang, kategori, stok, harga_beli, harga_jual, satuan) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps);
            ps.executeUpdate();
            AlertHelper.showInfo("Sukses", "Barang berhasil ditambahkan.");
        } catch (SQLException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    private void updateBarang() {
        String sql = "UPDATE barang SET kode_barang=?, nama_barang=?, kategori=?, stok=?, "
                   + "harga_beli=?, harga_jual=?, satuan=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps);
            ps.setInt(8, selectedBarang.getId());
            ps.executeUpdate();
            AlertHelper.showInfo("Sukses", "Barang berhasil diperbarui.");
        } catch (SQLException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    private void fillStatement(PreparedStatement ps) throws SQLException {
        ps.setString(1, txtKode.getText().trim());
        ps.setString(2, txtNama.getText().trim());
        ps.setString(3, txtKategori.getText().trim());
        ps.setInt(4, Integer.parseInt(txtStok.getText().trim()));
        ps.setDouble(5, Double.parseDouble(txtHargaBeli.getText().trim()));
        ps.setDouble(6, Double.parseDouble(txtHargaJual.getText().trim()));
        ps.setString(7, txtSatuan.getText().trim());
    }

    private boolean validateForm() {
        if (Validator.isEmpty(txtKode.getText()) || Validator.isEmpty(txtNama.getText())) {
            AlertHelper.showWarning("Validasi", "Kode dan nama barang wajib diisi!");
            return false;
        }
        if (!Validator.isValidInteger(txtStok.getText())) {
            AlertHelper.showWarning("Validasi", "Stok harus berupa angka!");
            return false;
        }
        if (!Validator.isValidDouble(txtHargaBeli.getText()) || !Validator.isValidDouble(txtHargaJual.getText())) {
            AlertHelper.showWarning("Validasi", "Harga beli/jual harus berupa angka!");
            return false;
        }
        return true;
    }

    private void populateForm(Barang b) {
        selectedBarang = b;
        txtKode.setText(b.getKodeBarang());
        txtNama.setText(b.getNamaBarang());
        txtKategori.setText(b.getKategori());
        txtStok.setText(String.valueOf(b.getStok()));
        txtHargaBeli.setText(String.valueOf(b.getHargaBeli()));
        txtHargaJual.setText(String.valueOf(b.getHargaJual()));
        txtSatuan.setText(b.getSatuan());
    }

    private void clearForm() {
        selectedBarang = null;
        txtKode.clear(); txtNama.clear(); txtKategori.clear();
        txtStok.clear(); txtHargaBeli.clear(); txtHargaJual.clear(); txtSatuan.clear();
        tblBarang.getSelectionModel().clearSelection();
    }

    private Barang mapResultSet(ResultSet rs) throws SQLException {
        return new Barang(
            rs.getInt("id"),
            rs.getString("kode_barang"),
            rs.getString("nama_barang"),
            rs.getString("kategori"),
            rs.getInt("stok"),
            rs.getDouble("harga_beli"),
            rs.getDouble("harga_jual"),
            rs.getString("satuan")
        );
    }
}
