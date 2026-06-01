package com.parking.dao;

import com.parking.config.Database;
import com.parking.model.Car;
import com.parking.model.Slot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlotDao {
    public List<Slot> findAll() {
        List<Slot> slots = new ArrayList<>();
        String sql = "SELECT s.id, s.slot_code, s.floor, s.type, s.status, "
                + "c.id AS car_id, c.registration_no, c.owner_name, c.model, c.car_type, c.color, c.status AS car_status "
                + "FROM slots s LEFT JOIN cars c ON c.id = s.car_id ORDER BY s.slot_code";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                slots.add(mapSlot(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load slots.", exception);
        }
        return slots;
    }

    public void allocate(int slotId, int carId) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!exists(connection, "cars", carId)) {
                    throw new IllegalArgumentException("Selected car does not exist.");
                }
                if (!slotAvailable(connection, slotId)) {
                    throw new IllegalArgumentException("Selected slot is not available.");
                }
                String occupiedSlot = occupiedSlotForCar(connection, carId);
                if (occupiedSlot != null) {
                    throw new IllegalArgumentException("This car is already parked in slot " + occupiedSlot + ".");
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE slots SET status = 'OCCUPIED', car_id = ?, updated_at = ? WHERE id = ?")) {
                    statement.setInt(1, carId);
                    statement.setString(2, LocalDateTime.now().toString());
                    statement.setInt(3, slotId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE cars SET status = 'PARKED' WHERE id = ?")) {
                    statement.setInt(1, carId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (RuntimeException | SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to allocate slot.", exception);
        }
    }

    public void release(int slotId) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer carId = carIdForSlot(connection, slotId);
                if (carId == null) {
                    throw new IllegalArgumentException("Selected slot is already available.");
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE slots SET status = 'AVAILABLE', car_id = NULL, updated_at = ? WHERE id = ?")) {
                    statement.setString(1, LocalDateTime.now().toString());
                    statement.setInt(2, slotId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE cars SET status = 'ACTIVE' WHERE id = ?")) {
                    statement.setInt(1, carId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (RuntimeException | SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to release slot.", exception);
        }
    }

    public boolean isCarAssigned(int carId) {
        String sql = "SELECT 1 FROM slots WHERE car_id = ? AND status = 'OCCUPIED' LIMIT 1";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, carId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to check car slot assignment.", exception);
        }
    }

    public void create(String slotCode, String floor, String type) {
        String sql = "INSERT INTO slots(slot_code, floor, type, status) VALUES (?, ?, ?, 'AVAILABLE')";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, slotCode);
            statement.setString(2, floor);
            statement.setString(3, type);
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("A slot with this code already exists.");
            }
            throw new IllegalStateException("Unable to create slot.", exception);
        }
    }

    public void delete(int slotId) {
        String sql = "DELETE FROM slots WHERE id = ? AND status = 'AVAILABLE' AND car_id IS NULL";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, slotId);
            int deleted = statement.executeUpdate();
            if (deleted == 0) {
                throw new IllegalArgumentException("Only available empty slots can be deleted.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to delete slot. Remove related bookings first.", exception);
        }
    }

    private boolean exists(Connection connection, String table, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean slotAvailable(Connection connection, int slotId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM slots WHERE id = ? AND status = 'AVAILABLE' AND car_id IS NULL")) {
            statement.setInt(1, slotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String occupiedSlotForCar(Connection connection, int carId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT slot_code FROM slots WHERE car_id = ? AND status = 'OCCUPIED' LIMIT 1")) {
            statement.setInt(1, carId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("slot_code") : null;
            }
        }
    }

    private Integer carIdForSlot(Connection connection, int slotId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT car_id FROM slots WHERE id = ? AND status = 'OCCUPIED'")) {
            statement.setInt(1, slotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                int carId = resultSet.getInt("car_id");
                return resultSet.wasNull() ? null : carId;
            }
        }
    }

    private Slot mapSlot(ResultSet resultSet) throws SQLException {
        Car car = null;
        int carId = resultSet.getInt("car_id");
        if (!resultSet.wasNull()) {
            car = new Car(carId, resultSet.getString("registration_no"), resultSet.getString("owner_name"),
                    resultSet.getString("model"), resultSet.getString("car_type"), resultSet.getString("color"),
                    resultSet.getString("car_status"));
        }
        return new Slot(resultSet.getInt("id"), resultSet.getString("slot_code"), resultSet.getString("floor"),
                resultSet.getString("type"), resultSet.getString("status"), car);
    }
}
