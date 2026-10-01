package org.example.humanity.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.humanity.Main;
import org.example.humanity.Session;
import org.example.humanity.dao.UsersDao;
import org.example.humanity.models.UsersModel;

import java.sql.SQLException;

public class AuthController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final UsersDao usersDao = new UsersDao();

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Введите логин и пароль");
            return;
        }

        UsersModel user;
        try {
            user = usersDao.findByUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Не удалось подключиться к базе данных");
            return;
        }

        // Пароль хранится в базе как есть, без хэширования
        if (user == null || !user.getPassword_hash().equals(password)) {
            errorLabel.setText("Неверный логин или пароль");
            passwordField.clear();
            return;
        }

        Session.login(user);
        Main.showView("main-menu-view.fxml", 420, 280);
    }

    @FXML
    void onExit(ActionEvent event) {
        System.exit(0);
    }
}
