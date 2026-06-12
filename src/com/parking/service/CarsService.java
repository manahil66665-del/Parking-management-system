package com.parking.service;

import com.parking.dao.CarDao;
import com.parking.model.Car;
import com.parking.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class CarsService {
    private final CarDao carDao = new CarDao();
    private final SlotService slotService = new SlotService();

    public List<Car> allCars() {
        return carDao.findAll();
    }

    public Optional<Car> findByRegistration(String registrationNo) {
        ValidationUtil.requireText(registrationNo, "Registration number");
        return carDao.findByRegistration(registrationNo.trim());
    }

    public void createCar(String registrationNo, String ownerName, String model, String carType, String color) {
        ValidationUtil.requireText(registrationNo, "Registration number");
        ValidationUtil.requireText(ownerName, "Owner name");
        ValidationUtil.requireText(model, "Model");
        ValidationUtil.requireText(carType, "Car type");
        String normalizedType = carType.trim().toUpperCase();
        if (!normalizedType.equals("TRUCK") && !normalizedType.equals("SEDAN") && !normalizedType.equals("JEEP")
                && !normalizedType.equals("OTHER")) {
            throw new IllegalArgumentException("Car type must be TRUCK, SEDAN, JEEP, or OTHER.");
        }
        carDao.create(new Car(0, registrationNo.trim().toUpperCase(), ownerName.trim(), model.trim(), normalizedType,
                color, "ACTIVE"));
    }

    public void deleteCar(int carId) {
        if (slotService.isCarAssigned(carId)) {
            throw new IllegalArgumentException("This car is currently parked. Release its slot before deleting it.");
        }
        carDao.delete(carId);
    }
}
