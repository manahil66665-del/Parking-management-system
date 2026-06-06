package com.parking.service;

import com.parking.dao.BillDao;
import com.parking.dao.CarDao;
import com.parking.model.Bill;
import com.parking.model.Car;

import java.util.List;
import java.util.Map;

public class BillingService {
    private static final Map<String, Double> DAILY_RATES = Map.of(
            "SEDAN", 300.0,
            "JEEP", 400.0,
            "TRUCK", 600.0,
            "OTHER", 250.0);

    private final BillDao billDao = new BillDao();
    private final CarDao carDao = new CarDao();

    public List<Bill> allBills() {
        return billDao.findAll();
    }

    public Bill createBill(int carId, int billedDays, String notes) {
        if (billedDays < 1) {
            throw new IllegalArgumentException("Billing days must be at least 1.");
        }
        Car car = carDao.findById(carId).orElseThrow(() -> new IllegalArgumentException("Selected car does not exist."));
        double dailyRate = dailyRateFor(car.getCarType());
        double amount = calculateAmount(car.getCarType(), billedDays);
        return billDao.create(carId, dailyRate, billedDays, amount, notes);
    }

    public double dailyRateFor(String carType) {
        return DAILY_RATES.getOrDefault(carType == null ? "OTHER" : carType.toUpperCase(), DAILY_RATES.get("OTHER"));
    }

    public double calculateAmount(String carType, int billedDays) {
        return dailyRateFor(carType) * Math.max(1, billedDays);
    }

    public void markPaid(int billId) {
        billDao.markPaid(billId);
    }

    public void deleteBill(int billId) {
        billDao.delete(billId);
    }
}
