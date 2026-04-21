package com.codearena.controller;

import com.codearena.service.AuthService;
import com.codearena.util.AuthException;
import com.codearena.util.NavigationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    private final AuthService authService;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    public RegisterController() {
        this(new AuthService());
    }

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void initialize() {
        hideError();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        hideError();

        if (!passwordsMatch()) {
            showError("Passwords do not match.");
            return;
        }

        try {
            boolean registered = authService.register(
                    usernameField.getText(),
                    emailField.getText(),
                    passwordField.getText()
            );

            if (registered) {
                NavigationUtil.setFlashMessage("Registration successful. Please log in.");
                NavigationUtil.navigateTo("login.fxml", event);
            } else {
                showError("Registration failed.");
            }
        } catch (AuthException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        NavigationUtil.navigateTo("login.fxml", event);
    }

    private boolean passwordsMatch() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        return password != null && password.equals(confirmPassword);
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
