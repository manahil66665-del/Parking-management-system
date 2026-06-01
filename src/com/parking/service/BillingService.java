package com.parking.service;

import com.parking.dao.BillDao;
import com.parking.model.Bill;
import com.parking.util.ValidationUtil;

import java.util.List;

public class BillingService {
    private final BillDao billDao = new BillDao();

    public List<Bill> allBills() {
        return billDao.findAll();
    }

    public Bill createBill(int carId, double amount, String notes) {
        ValidationUtil.requirePositive(amount, "Amount");
        return billDao.create(carId, amount, notes);
    }

    public void markPaid(int billId) {
        billDao.markPaid(billId);
    }

    public void deleteBill(int billId) {
        billDao.delete(billId);
    }
}
