package com.parking.dao;

import com.parking.config.Database;
import com.parking.model.Bill;
import com.parking.model.Car;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BillDao {
    public List<Bill> findAll() {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT b.id, b.invoice_no, b.daily_rate, b.billed_days, b.amount, b.status, b.issued_at, b.paid_at, b.notes, "
                + "c.id AS car_id, c.registration_no, c.owner_name, c.model, c.car_type, c.color, c.status AS car_status "
                + "FROM bills b JOIN cars c ON c.id = b.car_id ORDER BY b.issued_at DESC";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                bills.add(mapBill(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load bills.", exception);
        }
        return bills;
    }

    public Bill create(int carId, double dailyRate, int billedDays, double amount, String notes) {
        String invoiceNo = "INV-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
        String sql = "INSERT INTO bills(invoice_no, car_id, daily_rate, billed_days, amount, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, invoiceNo);
            statement.setInt(2, carId);
            statement.setDouble(3, dailyRate);
            statement.setInt(4, billedDays);
            statement.setDouble(5, amount);
            statement.setString(6, notes);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int billId = keys.getInt(1);
                    return findAll().stream().filter(bill -> bill.getId() == billId).findFirst()
                            .orElseThrow(() -> new IllegalStateException("Created bill not found."));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create bill.", exception);
        }
        throw new IllegalStateException("Bill was not created.");
    }

    public void markPaid(int billId) {
        String sql = "UPDATE bills SET status = 'PAID', paid_at = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LocalDateTime.now().toString());
            statement.setInt(2, billId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Selected invoice does not exist.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to mark bill as paid.", exception);
        }
    }

    public void delete(int billId) {
        String sql = "DELETE FROM bills WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, billId);
            int deleted = statement.executeUpdate();
            if (deleted == 0) {
                throw new IllegalArgumentException("Selected invoice does not exist.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to delete bill.", exception);
        }
    }

    public double unpaidTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM bills WHERE status = 'UNPAID'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getDouble("total") : 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to calculate unpaid total.", exception);
        }
    }

    private Bill mapBill(ResultSet resultSet) throws SQLException {
        Car car = new Car(resultSet.getInt("car_id"), resultSet.getString("registration_no"),
                resultSet.getString("owner_name"), resultSet.getString("model"), resultSet.getString("car_type"),
                resultSet.getString("color"), resultSet.getString("car_status"));
        return new Bill(resultSet.getInt("id"), resultSet.getString("invoice_no"), car,
                resultSet.getDouble("daily_rate"), resultSet.getInt("billed_days"), resultSet.getDouble("amount"),
                resultSet.getString("status"), resultSet.getString("issued_at"), resultSet.getString("paid_at"),
                resultSet.getString("notes"));
    }
}
