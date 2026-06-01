package com.parking.model;

public class Bill {
    private int id;
    private String invoiceNo;
    private Car car;
    private double amount;
    private String status;
    private String issuedAt;
    private String paidAt;
    private String notes;

    public Bill(int id, String invoiceNo, Car car, double amount, String status, String issuedAt, String paidAt,
            String notes) {
        this.id = id;
        this.invoiceNo = invoiceNo;
        this.car = car;
        this.amount = amount;
        this.status = status;
        this.issuedAt = issuedAt;
        this.paidAt = paidAt;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public Car getCar() {
        return car;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public String getNotes() {
        return notes;
    }
}
