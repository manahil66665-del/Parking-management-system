package com.parking.service;

import com.parking.dao.BillDao;
import com.parking.dao.DashboardDao;

import java.util.Map;

public class DashboardService {
    private final DashboardDao dashboardDao = new DashboardDao();
    private final BillDao billDao = new BillDao();

    public int carCount() {
        return dashboardDao.count("cars");
    }

    public int slotCount() {
        return dashboardDao.count("slots");
    }

    public int billCount() {
        return dashboardDao.count("bills");
    }

    public double unpaidTotal() {
        return billDao.unpaidTotal();
    }

    public Map<String, Integer> slotStatusCounts() {
        return dashboardDao.slotStatusCounts();
    }

    public Map<String, Integer> carTypeCounts() {
        return dashboardDao.carTypeCounts();
    }
}
