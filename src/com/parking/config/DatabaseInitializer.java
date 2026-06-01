package com.parking.config;

import com.parking.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = Database.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("CREATE TABLE IF NOT EXISTS roles ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL UNIQUE,"
                    + "description TEXT)");
            statement.execute("CREATE TABLE IF NOT EXISTS app_settings ("
                    + "key TEXT PRIMARY KEY,"
                    + "value TEXT NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT NOT NULL UNIQUE,"
                    + "password_hash TEXT NOT NULL,"
                    + "full_name TEXT NOT NULL,"
                    + "role_id INTEGER NOT NULL,"
                    + "active INTEGER NOT NULL DEFAULT 1,"
                    + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(role_id) REFERENCES roles(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS cars ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "registration_no TEXT NOT NULL UNIQUE,"
                    + "owner_name TEXT NOT NULL,"
                    + "model TEXT NOT NULL,"
                    + "car_type TEXT NOT NULL DEFAULT 'OTHER',"
                    + "color TEXT,"
                    + "status TEXT NOT NULL DEFAULT 'ACTIVE',"
                    + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            addColumnIfMissing(connection, "cars", "car_type", "TEXT NOT NULL DEFAULT 'OTHER'");
            statement.execute("CREATE TABLE IF NOT EXISTS slots ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "slot_code TEXT NOT NULL UNIQUE,"
                    + "floor TEXT NOT NULL,"
                    + "type TEXT NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'AVAILABLE',"
                    + "car_id INTEGER,"
                    + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(car_id) REFERENCES cars(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS bookings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "car_id INTEGER NOT NULL,"
                    + "slot_id INTEGER NOT NULL,"
                    + "user_id INTEGER NOT NULL,"
                    + "start_time TEXT NOT NULL,"
                    + "end_time TEXT,"
                    + "status TEXT NOT NULL DEFAULT 'ACTIVE',"
                    + "FOREIGN KEY(car_id) REFERENCES cars(id),"
                    + "FOREIGN KEY(slot_id) REFERENCES slots(id),"
                    + "FOREIGN KEY(user_id) REFERENCES users(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS bills ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "invoice_no TEXT NOT NULL UNIQUE,"
                    + "car_id INTEGER NOT NULL,"
                    + "booking_id INTEGER,"
                    + "amount REAL NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'UNPAID',"
                    + "issued_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "paid_at TEXT,"
                    + "notes TEXT,"
                    + "FOREIGN KEY(car_id) REFERENCES cars(id),"
                    + "FOREIGN KEY(booking_id) REFERENCES bookings(id))");
            seed(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Database initialization failed: " + exception.getMessage(), exception);
        }
    }

    private static void seed(Connection connection) throws SQLException {
        boolean freshData = countRows(connection, "users") == 0
                && countRows(connection, "cars") == 0
                && countRows(connection, "slots") == 0
                && countRows(connection, "bills") == 0
                && countRows(connection, "bookings") == 0;

        insertRole(connection, "ADMIN", "Full system access");
        insertRole(connection, "STAFF", "Daily parking operations");

        int adminRoleId = roleId(connection, "ADMIN");
        if (countRows(connection, "users") == 0) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users(username, password_hash, full_name, role_id) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, "admin");
                statement.setString(2, PasswordUtil.hashPassword("admin123"));
                statement.setString(3, "System Administrator");
                statement.setInt(4, adminRoleId);
                statement.executeUpdate();
            }
        }

        if (shouldSeed(connection, "sample_cars_seeded", "cars", freshData)) {
            insertCar(connection, "ABC-123", "Ali Khan", "Toyota Corolla", "SEDAN", "White", "PARKED");
            insertCar(connection, "LHR-7788", "Sara Ahmed", "Honda Civic", "SEDAN", "Black", "ACTIVE");
            insertCar(connection, "ICT-550", "Usman Raza", "Suzuki Alto", "OTHER", "Silver", "ACTIVE");
            saveSetting(connection, "sample_cars_seeded", "true");
        }
        updateCarType(connection, "ABC-123", "SEDAN");
        updateCarType(connection, "LHR-7788", "SEDAN");
        updateCarType(connection, "ICT-550", "OTHER");

        if (shouldSeed(connection, "sample_slots_seeded", "slots", freshData)) {
            for (int i = 1; i <= 12; i++) {
                String code = "A-" + String.format("%02d", i);
                if (!exists(connection, "slots", "slot_code", code)) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO slots(slot_code, floor, type, status) VALUES (?, ?, ?, ?)")) {
                        statement.setString(1, code);
                        statement.setString(2, "Ground");
                        statement.setString(3, i <= 2 ? "ACCESSIBLE" : "STANDARD");
                        statement.setString(4, "AVAILABLE");
                        statement.executeUpdate();
                    }
                }
            }
            saveSetting(connection, "sample_slots_seeded", "true");
        }
    }

    private static boolean shouldSeed(Connection connection, String settingKey, String table, boolean freshData)
            throws SQLException {
        if (settingExists(connection, settingKey)) {
            return false;
        }
        if (countRows(connection, table) > 0 || !freshData) {
            saveSetting(connection, settingKey, "true");
            return false;
        }
        return true;
    }

    private static void insertRole(Connection connection, String name, String description) throws SQLException {
        if (!exists(connection, "roles", "name", name)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO roles(name, description) VALUES (?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, description);
                statement.executeUpdate();
            }
        }
    }

    private static void insertCar(Connection connection, String reg, String owner, String model, String carType,
            String color, String status) throws SQLException {
        if (!exists(connection, "cars", "registration_no", reg)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO cars(registration_no, owner_name, model, car_type, color, status) VALUES (?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, reg);
                statement.setString(2, owner);
                statement.setString(3, model);
                statement.setString(4, carType);
                statement.setString(5, color);
                statement.setString(6, status);
                statement.executeUpdate();
            }
        }
    }

    private static void updateCarType(Connection connection, String reg, String carType) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE cars SET car_type = ? WHERE registration_no = ? AND (car_type IS NULL OR car_type = 'OTHER')")) {
            statement.setString(1, carType);
            statement.setString(2, reg);
            statement.executeUpdate();
        }
    }

    private static void addColumnIfMissing(Connection connection, String table, String column, String definition)
            throws SQLException {
        try (ResultSet resultSet = connection.createStatement().executeQuery("PRAGMA table_info(" + table + ")")) {
            while (resultSet.next()) {
                if (column.equalsIgnoreCase(resultSet.getString("name"))) {
                    return;
                }
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static int roleId(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM roles WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        throw new SQLException("Role not found: " + name);
    }

    private static boolean exists(Connection connection, String table, String column, String value) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE " + column + " = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static int countRows(Connection connection, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS total FROM " + table);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt("total") : 0;
        }
    }

    private static boolean settingExists(Connection connection, String key) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM app_settings WHERE key = ? LIMIT 1")) {
            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void saveSetting(Connection connection, String key, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO app_settings(key, value) VALUES (?, ?)")) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        }
    }
}
