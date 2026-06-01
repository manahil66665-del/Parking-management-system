package com.parking.model;

public class Booking {
    private int id;
    private Car car;
    private Slot slot;
    private User user;
    private String startTime;
    private String endTime;
    private String status;

    public Booking(int id, Car car, Slot slot, User user, String startTime, String endTime, String status) {
        this.id = id;
        this.car = car;
        this.slot = slot;
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public Slot getSlot() {
        return slot;
    }

    public User getUser() {
        return user;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }
}
