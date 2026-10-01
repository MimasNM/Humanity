package org.example.humanity.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.example.humanity.Alerts;
import org.example.humanity.Main;
import org.example.humanity.Session;
import org.example.humanity.dao.ProductsDao;
import org.example.humanity.models.ProductsModel;

import java.sql.SQLException;

public class ProductsController {

    @FXML
    private TableView<ProductsModel> table;

    @FXML
    private TableColumn<ProductsModel, Integer> idColumn;
    @FXML
    private TableColumn<ProductsModel, String> nameColumn;
    @FXML
    private TableColumn<ProductsModel, String> descriptionColumn;
    @FXML
    private TableColumn<ProductsModel, String> skuColumn;
    @FXML
    private TableColumn<ProductsModel, Double> priceColumn;
    @FXML
    private TableColumn<ProductsModel, Integer> quantityColumn;
    @FXML
    private TableColumn<ProductsModel, String> categoryColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField skuField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField categoryField;

    /** Форма и кнопки CRUD — скрываются целиком, если роль не admin. */
    @FXML
    private VBox formBox;

    @FXML
    private Label infoLabel;

    private final ProductsDao productsDao = new ProductsDao();
    private final ObservableList<ProductsModel> products = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (!Session.isLoggedIn()) {
            Main.showViewLater("auth-view.fxml", 380, 240);
            return;
        }

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        table.setItems(products);
        table.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> fillForm(newValue));

        boolean canEdit = Session.isAdmin();
        formBox.setVisible(canEdit);
        infoLabel.setVisible(!canEdit);

        reload();
    }

    @FXML
    private void onBack() {
        Main.showView("main-menu-view.fxml", 420, 280);
    }

    @FXML
    private void onAdd() {
        ProductsModel product = readForm();
        if (product == null) {
            return;
        }
        product.setId(0);
        try {
            productsDao.insert(product);
            reload();
            onClear();
            Alerts.info("Товар добавлен");
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    @FXML
    private void onUpdate() {
        ProductsModel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Сначала выберите строку в таблице");
            return;
        }
        ProductsModel product = readForm();
        if (product == null) {
            return;
        }
        product.setId(selected.getId());
        try {
            productsDao.update(product);
            reload();
            Alerts.info("Товар сохранён");
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    @FXML
    private void onDelete() {
        ProductsModel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Сначала выберите строку в таблице");
            return;
        }
        if (!Alerts.confirm("Удалить товар \"" + selected.getName() + "\"?")) {
            return;
        }
        try {
            productsDao.delete(selected.getId());
            reload();
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    @FXML
    private void onClear() {
        table.getSelectionModel().clearSelection();
        nameField.clear();
        descriptionField.clear();
        skuField.clear();
        priceField.clear();
        quantityField.clear();
        categoryField.clear();
    }

    private void reload() {
        try {
            products.setAll(productsDao.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
            Alerts.error(reason(e));
        }
    }

    /** Заполняет форму данными выбранной строки (или чистит, если строка не выбрана). */
    private void fillForm(ProductsModel product) {
        if (product == null) {
            onClear();
            return;
        }
        nameField.setText(product.getName());
        descriptionField.setText(nullToEmpty(product.getDescription()));
        skuField.setText(product.getSku());
        priceField.setText(String.valueOf(product.getPrice()));
        quantityField.setText(String.valueOf(product.getQuantity()));
        categoryField.setText(nullToEmpty(product.getCategory()));
    }

    /** Собирает товар из полей. Возвращает null, если поля заполнены неверно. */
    private ProductsModel readForm() {
        String name = nameField.getText().trim();
        String sku = skuField.getText().trim();
        if (name.isEmpty() || sku.isEmpty()) {
            Alerts.error("Заполните название и артикул (SKU)");
            return null;
        }

        double price;
        int quantity;
        try {
            // разрешаем и запятую, и точку
            price = Double.parseDouble(priceField.getText().trim().replace(',', '.'));
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            Alerts.error("Цена и количество должны быть числами");
            return null;
        }
        if (price < 0 || quantity < 0) {
            Alerts.error("Цена и количество не могут быть отрицательными");
            return null;
        }

        return new ProductsModel(0, name, descriptionField.getText(), sku, price, quantity, categoryField.getText());
    }

    private static String reason(SQLException e) {
        return "Ошибка базы данных: " + e.getMessage();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
