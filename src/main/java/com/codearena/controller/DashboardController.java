package com.codearena.controller;

import com.codearena.service.AuthService;
import com.codearena.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class DashboardController {

    private final AuthService authService;

    public DashboardController() {
        this(new AuthService());
    }

    public DashboardController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void goToProblems(ActionEvent event) {
        NavigationUtil.navigateTo("problem-list.fxml", event);
    }

    @FXML
    private void logout(ActionEvent event) {
        authService.logout();
        NavigationUtil.navigateTo("login.fxml", event);
    }
}
