package com.plasticstock.controllers;

import com.plasticstock.database.DatabaseConnection;
import com.plasticstock.models.User;
import com.plasticstock.utils.AlertHelper;
import com.plasticstock.utils.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.net.URL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnLogin;
    @FXML private MediaView     mediaView;
    @FXML private VBox          loginCard;
    @FXML private Pane          particleLayer;

    private boolean loginShown = false;

    @FXML
    public void initialize() {
        loginCard.setOpacity(0.0);
        mediaView.setOpacity(0.0);
        playParticleAnimations();

        // Setup Video Background
        URL mediaUrl = getClass().getResource("/com/plasticstock/images/bg-login.mp4");
        if (mediaUrl != null) {
            Media media = new Media(mediaUrl.toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.setPreserveRatio(false);

            // The mediaView size is typically bound to the scene in a complex layout, 
            // but StackPane will center it. To make it cover, we can bind it if we want.
            mediaView.fitWidthProperty().bind(javafx.beans.binding.Bindings.selectDouble(mediaView.sceneProperty(), "width"));
            mediaView.fitHeightProperty().bind(javafx.beans.binding.Bindings.selectDouble(mediaView.sceneProperty(), "height"));

            mediaPlayer.setOnPlaying(this::showLoginAfterVideoStarts);
            mediaPlayer.setOnError(this::showLoginCard);
            media.setOnError(this::showLoginCard);
            mediaPlayer.play();
        } else {
            showLoginCard();
        }
    }

    private void showLoginAfterVideoStarts() {
        if (loginShown) return;
        loginShown = true;

        FadeTransition videoFade = new FadeTransition(Duration.millis(250), mediaView);
        videoFade.setToValue(1.0);
        videoFade.setOnFinished(event -> showLoginCard());
        videoFade.play();
    }

    private void showLoginCard() {
        if (loginCard.getOpacity() > 0) return;

        FadeTransition ft = new FadeTransition(Duration.millis(1500), loginCard);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(120));
        ft.play();
    }

    private void playParticleAnimations() {
        for (int i = 0; i < particleLayer.getChildren().size(); i++) {
            Node particle = particleLayer.getChildren().get(i);

            TranslateTransition drift = new TranslateTransition(Duration.seconds(4 + i), particle);
            drift.setByY(-18 - (i * 3));
            drift.setByX(i % 2 == 0 ? 8 : -8);
            drift.setAutoReverse(true);
            drift.setCycleCount(TranslateTransition.INDEFINITE);
            drift.setDelay(Duration.millis(i * 260));
            drift.play();

            FadeTransition shimmer = new FadeTransition(Duration.seconds(3 + i), particle);
            shimmer.setFromValue(0.35);
            shimmer.setToValue(0.82);
            shimmer.setAutoReverse(true);
            shimmer.setCycleCount(FadeTransition.INDEFINITE);
            shimmer.setDelay(Duration.millis(i * 180));
            shimmer.play();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Validasi input
        if (Validator.isEmpty(username) || Validator.isEmpty(password)) {
            AlertHelper.showWarning("Peringatan", "Username dan password tidak boleh kosong!");
            return;
        }

        // Cek ke database
        User user = authenticate(username, password);
        if (user == null) {
            AlertHelper.showError("Login Gagal", "Username atau password salah!");
            return;
        }

        // Buka dashboard
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/plasticstock/views/dashboard.fxml")
            );
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                getClass().getResource("/com/plasticstock/css/style.css").toExternalForm()
            );

            DashboardController dashboardCtrl = loader.getController();
            dashboardCtrl.initUser(user);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard — Plastic Stock Manager");
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (IOException e) {
            AlertHelper.showError("Error", "Gagal membuka dashboard: " + e.getMessage());
        }
    }

    private User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password); // TODO: ganti dengan hash (BCrypt)

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", e.getMessage());
        }
        return null;
    }
}
