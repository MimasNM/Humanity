package org.example.humanity.dao;

import org.example.humanity.db.Database;
import org.example.humanity.models.UsersModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD для таблицы users. Вызывается только из UsersController, куда пускают лишь админа.
 */
public class UsersDao {

    private static final String COLUMNS = "id, username, email, password_hash, full_name, phone, role";

    public List<UsersModel> findAll() throws SQLException {
        List<UsersModel> users = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM users ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(read(resultSet));
            }
        }
        return users;
    }

    /** Возвращает пользователя или null, если логина нет. Используется при входе. */
    public UsersModel findByUsername(String username) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM users WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? read(resultSet) : null;
            }
        }
    }

    public int insert(UsersModel user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, phone, role)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            write(statement, user);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        }
        return user.getId();
    }

    public void update(UsersModel user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, full_name = ?, phone = ?, role = ?"
                + " WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            write(statement, user);
            statement.setInt(7, user.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private static UsersModel read(ResultSet resultSet) throws SQLException {
        return new UsersModel(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("role"));
    }

    private static void write(PreparedStatement statement, UsersModel user) throws SQLException {
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getEmail());
        statement.setString(3, user.getPassword_hash());
        // Пустые строки в nullable-колонки пишем как NULL, а не как ''
        statement.setString(4, nullIfEmpty(user.getFull_name()));
        statement.setString(5, nullIfEmpty(user.getPhone()));
        statement.setString(6, user.getRole());
    }

    private static String nullIfEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
