package com.parking.model;

public class Car {
    private int id;
    private String registrationNo;
    private String ownerName;
    private String model;
    private String carType;
    private String color;
    private String status;

    public Car(int id, String registrationNo, String ownerName, String model, String color, String status) {
        this(id, registrationNo, ownerName, model, "OTHER", color, status);
    }

    public Car(int id, String registrationNo, String ownerName, String model, String carType, String color,
            String status) {
        this.id = id;
        this.registrationNo = registrationNo;
        this.ownerName = ownerName;
        this.model = model;
        this.carType = carType;
        this.color = color;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getModel() {
        return model;
    }

    public String getCarType() {
        return carType;
    }

    public String getColor() {
        return color;
    }

    public String getStatus() {
        return status;
    }

    public String displayName() {
        return registrationNo + " - " + model + " (" + carType + ")";
    }
}
