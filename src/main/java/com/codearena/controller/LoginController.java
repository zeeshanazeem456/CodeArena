package com.codearena.controller;

import com.codearena.model.User;
import com.codearena.service.AuthService;
import com.codearena.util.AuthException;
import com.codearena.util.NavigationUtil;
import com.codearena.util.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private final AuthService authService;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    public LoginController() {
        this(new AuthService());
    }

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void initialize() {
        String flashMessage = NavigationUtil.consumeFlashMessage();
        if (flashMessage != null && !flashMessage.isBlank()) {
            showError(flashMessage);
        } else {
            hideError();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        hideError();

        try {
            User user = authService.login(usernameField.getText(), passwordField.getText());
            SessionManager.setCurrentUser(user);
            NavigationUtil.navigateTo("dashboard.fxml", event);
        } catch (AuthException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        NavigationUtil.navigateTo("register.fxml", event);
    }

    private void showError(String message) {
        Runnable uiUpdate = () -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        };

        if (Platform.isFxApplicationThread()) {
            uiUpdate.run();
        } else {
            Platform.runLater(uiUpdate);
        }
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
