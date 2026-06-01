package com.parking.model;

public class Slot {
    private int id;
    private String slotCode;
    private String floor;
    private String type;
    private String status;
    private Car car;

    public Slot(int id, String slotCode, String floor, String type, String status, Car car) {
        this.id = id;
        this.slotCode = slotCode;
        this.floor = floor;
        this.type = type;
        this.status = status;
        this.car = car;
    }

    public int getId() {
        return id;
    }

    public String getSlotCode() {
        return slotCode;
    }

    public String getFloor() {
        return floor;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public Car getCar() {
        return car;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }
}
