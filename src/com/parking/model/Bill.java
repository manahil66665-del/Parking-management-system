package com.parking.model;

public class Bill {
    private int id;
    private String invoiceNo;
    private Car car;
    private double dailyRate;
    private int BilledDays;
    private double amount;
    private String status;
    private String issuedAt;
    private String paidAt;
    private String notes;

    public Bill(int id, String invoiceNo, Car car, double amount, String status, String issuedAt, String paidAt,
                String notes) {
        this(id, invoiceNo, car, amount, 1, amount, status, issuedAt, paidAt, notes);
    }

    public Bill(int id, String invoiceNo, Car car, double dailyRate, int billedDays, double amount, String status,
                String issuedAt, String paidAt, String notes) {
        this.id = id;
        this.invoiceNo = invoiceNo;
        this.car = car;
        this.dailyRate = dailyRate;
        this.BilledDays = billedDays;
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

    public double getDailyRate() {
        return dailyRate;
    }

    public int getBilledDays() {
        return BilledDays;
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
