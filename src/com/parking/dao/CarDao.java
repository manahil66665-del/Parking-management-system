package com.parking.dao;

import com.parking.config.Database;
import com.parking.model.Car;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarDao {
    public List<Car> findAll() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT id, registration_no, owner_name, model, car_type, color, status FROM cars ORDER BY registration_no";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                cars.add(mapCar(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load cars.", exception);
        }
        return cars;
    }

    public Optional<Car> findById(int id) {
        String sql = "SELECT id, registration_no, owner_name, model, car_type, color, status FROM cars WHERE id = ?";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCar(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load car.", exception);
        }
        return Optional.empty();
    }

    public Optional<Car> findByRegistration(String registrationNo) {
        String sql = "SELECT id, registration_no, owner_name, model, car_type, color, status FROM cars "
                + "WHERE UPPER(registration_no) = UPPER(?)";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCar(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to search car.", exception);
        }
        return Optional.empty();
    }

    public void create(Car car) {
        String sql = "INSERT INTO cars(registration_no, owner_name, model, car_type, color, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, car.getRegistrationNo());
            statement.setString(2, car.getOwnerName());
            statement.setString(3, car.getModel());
            statement.setString(4, car.getCarType());
            statement.setString(5, car.getColor());
            statement.setString(6, car.getStatus());
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("A car with this registration number already exists.");
            }
            throw new IllegalStateException("Unable to create car.", exception);
        }
    }

    public void delete(int carId) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteRelated(connection, "bills", carId);
                deleteRelated(connection, "bookings", carId);
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM cars WHERE id = ?")) {
                    statement.setInt(1, carId);
                    int deleted = statement.executeUpdate();
                    if (deleted == 0) {
                        throw new IllegalArgumentException("Selected car does not exist.");
                    }
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
            throw new IllegalStateException("Unable to delete car.", exception);
        }
    }

    private void deleteRelated(Connection connection, String table, int carId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE car_id = ?")) {
            statement.setInt(1, carId);
            statement.executeUpdate();
        }
    }

    static Car mapCar(ResultSet resultSet) throws SQLException {
        return new Car(resultSet.getInt("id"), resultSet.getString("registration_no"),
                resultSet.getString("owner_name"), resultSet.getString("model"), resultSet.getString("car_type"),
                resultSet.getString("color"), resultSet.getString("status"));
    }
}
