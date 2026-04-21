package com.codearena.util;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class NavigationUtil {

    private static String flashMessage;

    private NavigationUtil() {
    }

    public static void navigateTo(String fxmlName, ActionEvent event) {
        navigateTo(fxmlName, (Node) event.getSource());
    }

    public static void navigateTo(String fxmlName, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/" + fxmlName));
            Parent root = loader.load();

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene currentScene = stage.getScene();
            if (currentScene == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.setScene(new Scene(root, currentScene.getWidth(), currentScene.getHeight()));
            }
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to navigate to " + fxmlName, exception);
        }
    }

    public static void setFlashMessage(String message) {
        flashMessage = message;
    }

    public static String consumeFlashMessage() {
        String message = flashMessage;
        flashMessage = null;
        return message;
    }
}
