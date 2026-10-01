package org.example.humanity.dao;

import org.example.humanity.db.Database;
import org.example.humanity.models.ProductsModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD для таблицы products.
 * Читать могут все залогиненные, править и удалять — только админ (это проверяет ProductsController).
 */
public class ProductsDao {

    private static final String COLUMNS = "id, name, description, sku, price, quantity, category";

    public List<ProductsModel> findAll() throws SQLException {
        List<ProductsModel> products = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM products ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(read(resultSet));
            }
        }
        return products;
    }

    public int insert(ProductsModel product) throws SQLException {
        String sql = "INSERT INTO products (name, description, sku, price, quantity, category)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            write(statement, product);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
        }
        return product.getId();
    }

    public void update(ProductsModel product) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, sku = ?, price = ?, quantity = ?, category = ?"
                + " WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            write(statement, product);
            statement.setInt(7, product.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private static ProductsModel read(ResultSet resultSet) throws SQLException {
        return new ProductsModel(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("sku"),
                resultSet.getDouble("price"),
                resultSet.getInt("quantity"),
                resultSet.getString("category"));
    }

    private static void write(PreparedStatement statement, ProductsModel product) throws SQLException {
        statement.setString(1, product.getName());
        // Пустые строки в nullable-колонки пишем как NULL, а не как ''
        statement.setString(2, nullIfEmpty(product.getDescription()));
        statement.setString(3, product.getSku());
        statement.setDouble(4, product.getPrice());
        statement.setInt(5, product.getQuantity());
        statement.setString(6, nullIfEmpty(product.getCategory()));
    }

    private static String nullIfEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
