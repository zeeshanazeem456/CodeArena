package com.codearena.util;

import com.codearena.ui.ScreenFactory;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class NavigationUtil {

    private static String flashMessage;

    private NavigationUtil() {
    }

    public static void navigateTo(String screenName, ActionEvent event) {
        navigateTo(screenName, (Node) event.getSource());
    }

    public static void navigateTo(String screenName, Node sourceNode) {
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        Scene currentScene = stage.getScene();
        Scene nextScene;
        if (currentScene == null) {
            nextScene = new Scene(ScreenFactory.create(screenName));
        } else {
            nextScene = new Scene(ScreenFactory.create(screenName), currentScene.getWidth(), currentScene.getHeight());
        }
        stage.setScene(nextScene);
        stage.show();
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
