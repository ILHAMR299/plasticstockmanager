package com.plasticstock;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load custom fonts
        loadFont("/com/plasticstock/font/Cinzel.ttf");
        loadFont("/com/plasticstock/font/Cinzel-Bold.ttf");
        loadFont("/com/plasticstock/font/Quicksand.ttf");
        loadFont("/com/plasticstock/font/NotoSans.ttf");
        loadFont("/com/plasticstock/font/BitcountSingle.ttf");
        loadFont("/com/plasticstock/font/Minecraft.ttf");

        FXMLLoader fxmlLoader = new FXMLLoader(
            Main.class.getResource("/com/plasticstock/views/login.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.getStylesheets().add(
            Main.class.getResource("/com/plasticstock/css/style.css").toExternalForm()
        );

        stage.setTitle("Manajemen stok toko plastik");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    private void loadFont(String resourcePath) {
        try (InputStream fontStream = Main.class.getResourceAsStream(resourcePath)) {
            if (fontStream != null) {
                Font.loadFont(fontStream, 14);
            }
        } catch (IOException ignored) {
            // Optional UI fonts can be absent during development.
        }
    }
}
