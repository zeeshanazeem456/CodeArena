package com.codearena;

import com.codearena.ui.ScreenFactory;
import com.codearena.util.PersistenceHandler;
import java.net.URL;
import java.sql.SQLException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

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
            showStartupSplash(primaryStage);
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
        URL iconUrl = MainApp.class.getResource("/images/logo-mark.png");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }
    }

    private void showStartupSplash(Stage primaryStage) {
        Stage splash = new Stage(StageStyle.UNDECORATED);
        splash.setTitle("CodeArena");
        loadApplicationIcon(splash);
        Image firstFrame = image("/images/title_screen.png");
        Image secondFrame = image("/images/title_screen_2.png");
        ImageView titleScreen = splashImageView(firstFrame);
        splash.setScene(new Scene(splashContent(titleScreen), 760, 480));
        splash.show();
        splash.centerOnScreen();

        Timeline frameSwap = new Timeline(
                new KeyFrame(Duration.ZERO, event -> titleScreen.setImage(firstFrame)),
                new KeyFrame(Duration.seconds(0.45), event -> titleScreen.setImage(secondFrame)),
                new KeyFrame(Duration.seconds(0.9), event -> titleScreen.setImage(firstFrame))
        );
        frameSwap.setCycleCount(Timeline.INDEFINITE);
        frameSwap.play();

        Timeline loading = new Timeline(new KeyFrame(Duration.seconds(2.7), event -> {
            frameSwap.stop();
            splash.close();
            primaryStage.show();
        }));
        loading.setCycleCount(1);
        loading.play();
    }

    private StackPane splashContent(ImageView titleScreen) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #06101A;");
        root.getChildren().add(titleScreen);
        return root;
    }

    private ImageView splashImageView(Image image) {
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitWidth(760);
        imageView.setFitHeight(480);
        imageView.setPreserveRatio(false);
        return imageView;
    }

    private Image image(String resource) {
        URL url = MainApp.class.getResource(resource);
        if (url != null) {
            return new Image(url.toExternalForm());
        }
        return null;
    }
}
