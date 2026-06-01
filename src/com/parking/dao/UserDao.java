package com.parking.dao;

import com.parking.config.Database;
import com.parking.model.Role;
import com.parking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.password_hash, u.full_name, u.active, "
                + "r.id AS role_id, r.name AS role_name, r.description "
                + "FROM users u JOIN roles r ON r.id = u.role_id ORDER BY u.username";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load users.", exception);
        }
        return users;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.id, u.username, u.password_hash, u.full_name, u.active, "
                + "r.id AS role_id, r.name AS role_name, r.description "
                + "FROM users u JOIN roles r ON r.id = u.role_id WHERE u.username = ?";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load user.", exception);
        }
        return Optional.empty();
    }

    public void create(String username, String passwordHash, String fullName, String roleName) {
        String sql = "INSERT INTO users(username, password_hash, full_name, role_id) "
                + "VALUES (?, ?, ?, (SELECT id FROM roles WHERE name = ?))";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, fullName);
            statement.setString(4, roleName);
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("Username already exists.");
            }
            throw new IllegalStateException("Unable to create user.", exception);
        }
    }

    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, active ? 1 : 0);
            statement.setInt(2, userId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Selected user does not exist.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update user.", exception);
        }
    }

    public void update(int userId, String username, String fullName, String roleName, String passwordHash) {
        String sqlWithPassword = "UPDATE users SET username = ?, full_name = ?, "
                + "role_id = (SELECT id FROM roles WHERE name = ?), password_hash = ? WHERE id = ?";
        String sqlWithoutPassword = "UPDATE users SET username = ?, full_name = ?, "
                + "role_id = (SELECT id FROM roles WHERE name = ?) WHERE id = ?";
        boolean changePassword = passwordHash != null && !passwordHash.isBlank();
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        changePassword ? sqlWithPassword : sqlWithoutPassword)) {
            statement.setString(1, username);
            statement.setString(2, fullName);
            statement.setString(3, roleName);
            if (changePassword) {
                statement.setString(4, passwordHash);
                statement.setInt(5, userId);
            } else {
                statement.setInt(4, userId);
            }
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Selected user does not exist.");
            }
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("Username already exists.");
            }
            throw new IllegalStateException("Unable to update user.", exception);
        }
    }

    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            int deleted = statement.executeUpdate();
            if (deleted == 0) {
                throw new IllegalArgumentException("Selected user does not exist.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to delete user. Remove related bookings first.", exception);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Role role = new Role(resultSet.getInt("role_id"), resultSet.getString("role_name"),
                resultSet.getString("description"));
        return new User(resultSet.getInt("id"), resultSet.getString("username"),
                resultSet.getString("password_hash"), resultSet.getString("full_name"), role,
                resultSet.getInt("active") == 1);
    }
}
