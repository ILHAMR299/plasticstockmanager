package com.plasticstock.controllers;

import com.plasticstock.database.DatabaseConnection;
import com.plasticstock.models.User;
import com.plasticstock.models.Transaksi;
import com.plasticstock.utils.AlertHelper;
import com.plasticstock.utils.FormatUtil;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblClock;
    @FXML private Label lblDate;
    @FXML private Label lblTotalBarang;
    @FXML private Label lblStokMenipis;
    @FXML private Label lblTransaksiHariIni;
    
    @FXML private VBox dashboardView;
    @FXML private AnchorPane contentPane;
    
    @FXML private VBox cardTotal;
    @FXML private VBox cardLow;
    @FXML private VBox cardToday;

    @FXML private Button btnDashboard;
    @FXML private Button btnBarang;
    @FXML private Button btnTransaksi;
    
    @FXML private TableView<Transaksi> tableRecent;
    @FXML private TableColumn<Transaksi, Double> colTotal;

    private User currentUser;
    private Timeline realtimeUpdateTimeline;
    private ObservableList<Transaksi> recentTransactions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startLiveClock();
        tableRecent.setItems(recentTransactions);
        setupTableFormatting();
        
        loadStats();
        loadRecentTransactions();
        
        startRealtimeUpdates();
        setupHoverAnimations();
        playEntranceAnimations();
    }

    public void initUser(User user) {
        this.currentUser = user;
        lblWelcome.setText("Welcome, Ilham Romadhon");
    }

    private void startLiveClock() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
            "EEEE, dd MMMM yyyy",
            java.util.Locale.forLanguageTag("id-ID")
        );
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblClock.setText(now.format(timeFormatter));
            lblDate.setText(now.format(dateFormatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    
    private void startRealtimeUpdates() {
        // Poll database every 5 seconds for dashboard realtime effect
        realtimeUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            if (dashboardView.isVisible()) {
                loadStats();
                loadRecentTransactions();
            }
        }));
        realtimeUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        realtimeUpdateTimeline.play();
    }
    
    private void playEntranceAnimations() {
        VBox[] cards = {cardTotal, cardLow, cardToday};
        for (int i = 0; i < cards.length; i++) {
            cards[i].setOpacity(0);
            cards[i].setTranslateY(20);
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), cards[i]);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(200 + (i * 100)));
            ft.setInterpolator(Interpolator.EASE_BOTH);
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), cards[i]);
            tt.setToY(0);
            tt.setDelay(Duration.millis(200 + (i * 100)));
            tt.setInterpolator(Interpolator.EASE_OUT);
            
            ft.play();
            tt.play();
        }
        
        tableRecent.setOpacity(0);
        FadeTransition ftTable = new FadeTransition(Duration.millis(800), tableRecent);
        ftTable.setToValue(1);
        ftTable.setDelay(Duration.millis(600));
        ftTable.setInterpolator(Interpolator.EASE_BOTH);
        ftTable.play();
    }

    private void setupTableFormatting() {
        colTotal.setCellFactory(column -> new TableCell<Transaksi, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : FormatUtil.rupiah(value));
            }
        });
    }

    private void setupHoverAnimations() {
        Node[] animatedNodes = {cardTotal, cardLow, cardToday, btnDashboard, btnBarang, btnTransaksi};
        for (Node node : animatedNodes) {
            node.setOnMouseEntered(event -> scaleNode(node, 1.018));
            node.setOnMouseExited(event -> scaleNode(node, 1.0));
        }
    }

    private void scaleNode(Node node, double scale) {
        javafx.animation.ScaleTransition transition = new javafx.animation.ScaleTransition(Duration.millis(160), node);
        transition.setToX(scale);
        transition.setToY(scale);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.play();
    }

    @FXML
    private void goToDashboard() {
        contentPane.setVisible(false);
        contentPane.setManaged(false);
        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        setActiveNav(btnDashboard);
        loadStats();
        loadRecentTransactions();
        playEntranceAnimations();
    }

    @FXML
    private void goToBarang() {
        loadView("/com/plasticstock/views/barang.fxml");
    }

    @FXML
    private void goToTransaksi() {
        loadView("/com/plasticstock/views/transaksi.fxml");
    }

    @FXML
    private void handleLogout() {
        if (!AlertHelper.showConfirmation("Logout", "Yakin ingin keluar?")) return;
        
        if (realtimeUpdateTimeline != null) {
            realtimeUpdateTimeline.stop();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/plasticstock/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                getClass().getResource("/com/plasticstock/css/style.css").toExternalForm()
            );
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login — Plastic Stock Manager");
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertHelper.showError("Error", "Gagal membuka halaman login:\n" + e.getMessage());
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane view = loader.load();
            
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
            contentPane.setVisible(true);
            contentPane.setManaged(true);
            setActiveNav(fxmlPath.contains("barang") ? btnBarang : btnTransaksi);
            
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            AlertHelper.showError("Error", "Gagal memuat halaman:\n" + e.getMessage());
        }
    }

    private void setActiveNav(Button activeButton) {
        Button[] navButtons = {btnDashboard, btnBarang, btnTransaksi};
        for (Button button : navButtons) {
            button.getStyleClass().remove("sidebar-btn-active");
            if (button == activeButton && !button.getStyleClass().contains("sidebar-btn-active")) {
                button.getStyleClass().add("sidebar-btn-active");
            }
        }
    }

    private void loadStats() {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS total FROM barang");
                int totalBarang = rs1.next() ? rs1.getInt("total") : 0;

                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) AS total FROM barang WHERE stok <= 10");
                int stokMenipis = rs2.next() ? rs2.getInt("total") : 0;

                ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) AS total FROM transaksi WHERE DATE(tanggal) = CURDATE()");
                int transaksiHariIni = rs3.next() ? rs3.getInt("total") : 0;

                Platform.runLater(() -> {
                    updateLabelWithAnimation(lblTotalBarang, String.valueOf(totalBarang));
                    updateLabelWithAnimation(lblStokMenipis, String.valueOf(stokMenipis));
                    updateLabelWithAnimation(lblTransaksiHariIni, String.valueOf(transaksiHariIni));
                });

            } catch (Exception e) {
                System.out.println("Load stats error: " + e.getMessage());
            }
        });
    }

    private void updateLabelWithAnimation(Label label, String newValue) {
        if (!label.getText().equals(newValue)) {
            label.setText(newValue);
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(400), label);
            st.setFromX(1.5);
            st.setFromY(1.5);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        }
    }
    
    private void loadRecentTransactions() {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                 
                String sql = "SELECT t.*, b.nama_barang FROM transaksi t " +
                             "JOIN barang b ON t.barang_id = b.id " +
                             "ORDER BY t.tanggal DESC LIMIT 15";
                             
                ResultSet rs = stmt.executeQuery(sql);
                java.util.List<Transaksi> newList = new java.util.ArrayList<>();
                
                while (rs.next()) {
                    Transaksi t = new Transaksi();
                    t.setId(rs.getInt("id"));
                    t.setNoTransaksi(rs.getString("no_transaksi"));
                    t.setJenisTransaksi(rs.getString("jenis_transaksi"));
                    t.setBarangId(rs.getInt("barang_id"));
                    t.setNamaBarang(rs.getString("nama_barang"));
                    t.setJumlah(rs.getInt("jumlah"));
                    t.setHargaSatuan(rs.getDouble("harga_satuan"));
                    t.setTotalHarga(rs.getDouble("total_harga"));
                    t.setKeterangan(rs.getString("keterangan"));
                    t.setTanggal(rs.getTimestamp("tanggal").toLocalDateTime());
                    
                    newList.add(t);
                }
                
                Platform.runLater(() -> {
                    boolean changed = true;
                    if (!recentTransactions.isEmpty() && !newList.isEmpty()) {
                        // Check if the most recent transaction matches
                        if (recentTransactions.get(0).getId() == newList.get(0).getId() && recentTransactions.size() == newList.size()) {
                            changed = false;
                        }
                    } else if (recentTransactions.isEmpty() && newList.isEmpty()) {
                        changed = false;
                    }
                    
                    if (changed) {
                        recentTransactions.setAll(newList);
                        FadeTransition ft = new FadeTransition(Duration.millis(300), tableRecent);
                        ft.setFromValue(0.3);
                        ft.setToValue(1.0);
                        ft.play();
                    }
                });
            } catch (Exception e) {
                System.out.println("Load transactions error: " + e.getMessage());
            }
        });
    }
}
