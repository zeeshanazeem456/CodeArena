package com.codearena;

import com.codearena.ui.ScreenFactory;
import com.codearena.util.PersistenceHandler;
import java.net.URL;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            PersistenceHandler.initialize();

            Scene scene = new Scene(ScreenFactory.create("login"), 1100, 720);

            primaryStage.setTitle("CodeArena");
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.setScene(scene);
            loadApplicationIcon(primaryStage);
            primaryStage.show();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to launch CodeArena.", exception);
        }
    }

    @Override
    public void stop() throws Exception {
        PersistenceHandler.shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void loadApplicationIcon(Stage stage) {
        URL iconUrl = MainApp.class.getResource("/images/logo.png");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }
    }
}
