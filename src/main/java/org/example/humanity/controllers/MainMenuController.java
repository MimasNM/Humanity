package org.example.humanity.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.humanity.Main;
import org.example.humanity.Session;
import org.example.humanity.models.UsersModel;

public class MainMenuController {

    @FXML
    private Label userLabel;

    @FXML
    private Button usersButton;

    @FXML
    private void initialize() {
        if (!Session.isLoggedIn()) {
            Main.showViewLater("auth-view.fxml", 380, 240);
            return;
        }

        UsersModel user = Session.getCurrentUser();
        userLabel.setText("Вы вошли как " + user.getUsername() + " (" + user.getRole() + ")");

        // Пользователю с role = user таблица users недоступна вообще
        usersButton.setVisible(Session.isAdmin());
    }

    @FXML
    private void onProducts() {
        Main.showView("products-view.fxml", 900, 600);
    }

    @FXML
    private void onUsers() {
        if (!Session.isAdmin()) {
            return;
        }
        Main.showView("users-view.fxml", 950, 620);
    }

    @FXML
    private void onLogout() {
        Session.logout();
        Main.showView("auth-view.fxml", 380, 240);
    }
}
