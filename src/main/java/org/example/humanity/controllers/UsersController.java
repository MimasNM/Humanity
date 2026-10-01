package org.example.humanity.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.example.humanity.Alerts;
import org.example.humanity.Main;
import org.example.humanity.Session;
import org.example.humanity.dao.UsersDao;
import org.example.humanity.models.UsersModel;

import java.sql.SQLException;

/**
 * Полный CRUD по таблице users.
 * Сюда можно попасть только с ролью admin — это проверяется здесь, а не только в главном меню.
 */
public class UsersController {

    @FXML
    private TableView<UsersModel> table;

    @FXML
    private TableColumn<UsersModel, Integer> idColumn;
    @FXML
    private TableColumn<UsersModel, String> usernameColumn;
    @FXML
    private TableColumn<UsersModel, String> emailColumn;
    @FXML
    private TableColumn<UsersModel, String> passwordColumn;
    @FXML
    private TableColumn<UsersModel, String> fullNameColumn;
    @FXML
    private TableColumn<UsersModel, String> phoneColumn;
    @FXML
    private TableColumn<UsersModel, String> roleColumn;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> roleField;

    @FXML
    private VBox formBox;

    private final UsersDao usersDao = new UsersDao();
    private final ObservableList<UsersModel> users = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (!Session.isAdmin()) {
            // на случай если попасть сюда в обход главного меню
            Main.showViewLater("main-menu-view.fxml", 420, 280);
            return;
        }

        roleField.setItems(FXCollections.observableArrayList("user", "admin"));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password_hash"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("full_name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        table.setItems(users);
        table.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> fillForm(newValue));

        reload();
    }

    @FXML
    private void onBack() {
        Main.showView("main-menu-view.fxml", 420, 280);
    }

    @FXML
    private void onAdd() {
        UsersModel user = readForm();
        if (user == null) {
            return;
        }
        user.setId(0);
        try {
            usersDao.insert(user);
            reload();
            onClear();
            Alerts.info("Пользователь добавлен");
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    @FXML
    private void onUpdate() {
        UsersModel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Сначала выберите строку в таблице");
            return;
        }
        UsersModel user = readForm();
        if (user == null) {
            return;
        }
        user.setId(selected.getId());
        try {
            usersDao.update(user);
            reload();

            // если админ изменил сам себя — обновляем данные сессии
            if (user.getId() == Session.getCurrentUser().getId()) {
                Session.login(user);
            }
            Alerts.info("Пользователь сохранён");
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    @FXML
    private void onDelete() {
        UsersModel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Сначала выберите строку в таблице");
            return;
        }
        if (selected.getId() == Session.getCurrentUser().getId()) {
            Alerts.error("Нельзя удалить самого себя");
            return;
        }
        if (!Alerts.confirm("Удалить пользователя \"" + selected.getUsername() + "\"?")) {
            return;
        }
        try {
            usersDao.delete(selected.getId());
            reload();
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    @FXML
    private void onClear() {
        table.getSelectionModel().clearSelection();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        fullNameField.clear();
        phoneField.clear();
        roleField.getSelectionModel().clearSelection();
    }

    private void reload() {
        try {
            users.setAll(usersDao.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    private void fillForm(UsersModel user) {
        if (user == null) {
            onClear();
            return;
        }
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        passwordField.setText(nullToEmpty(user.getPassword_hash()));
        fullNameField.setText(nullToEmpty(user.getFull_name()));
        phoneField.setText(nullToEmpty(user.getPhone()));
        roleField.setValue(user.getRole());
    }

    /** Собирает пользователя из полей. Возвращает null, если поля заполнены неверно. */
    private UsersModel readForm() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleField.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Alerts.error("Заполните логин, email и пароль");
            return null;
        }
        if (!email.contains("@")) {
            Alerts.error("Email должен содержать @");
            return null;
        }
        if (role == null) {
            Alerts.error("Выберите роль");
            return null;
        }

        return new UsersModel(0, username, email, password, fullNameField.getText(), phoneField.getText(), role);
    }

    private static String reason(SQLException e) {
        return "Ошибка базы данных: " + e.getMessage();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
