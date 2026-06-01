package com.parking.dao;

import com.parking.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDao {
    public int count(String table) {
        String sql = "SELECT COUNT(*) AS total FROM " + table;
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt("total") : 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to calculate total for " + table, exception);
        }
    }

    public Map<String, Integer> slotStatusCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("AVAILABLE", 0);
        counts.put("OCCUPIED", 0);
        counts.put("MAINTENANCE", 0);
        String sql = "SELECT status, COUNT(*) AS total FROM slots GROUP BY status";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                counts.put(resultSet.getString("status"), resultSet.getInt("total"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load slot metrics.", exception);
        }
        return counts;
    }

    public Map<String, Integer> carTypeCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("SEDAN", 0);
        counts.put("JEEP", 0);
        counts.put("TRUCK", 0);
        counts.put("OTHER", 0);
        String sql = "SELECT car_type, COUNT(*) AS total FROM cars GROUP BY car_type";
        try (Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                counts.put(resultSet.getString("car_type"), resultSet.getInt("total"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load car type metrics.", exception);
        }
        return counts;
    }
}
