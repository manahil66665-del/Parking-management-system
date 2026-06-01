package com.parking.service;

import com.parking.dao.SlotDao;
import com.parking.model.Slot;
import com.parking.util.ValidationUtil;

import java.util.List;

public class SlotService {
    private final SlotDao slotDao = new SlotDao();

    public List<Slot> allSlots() {
        return slotDao.findAll();
    }

    public void allocate(int slotId, int carId) {
        slotDao.allocate(slotId, carId);
    }

    public void release(int slotId) {
        slotDao.release(slotId);
    }

    public boolean isCarAssigned(int carId) {
        return slotDao.isCarAssigned(carId);
    }

    public void createSlot(String slotCode, String floor, String type) {
        ValidationUtil.requireText(slotCode, "Slot code");
        ValidationUtil.requireText(floor, "Floor");
        ValidationUtil.requireText(type, "Slot type");
        String normalizedType = type.trim().toUpperCase();
        if (!normalizedType.equals("STANDARD") && !normalizedType.equals("ACCESSIBLE")
                && !normalizedType.equals("VIP") && !normalizedType.equals("LARGE")) {
            throw new IllegalArgumentException("Slot type must be STANDARD, ACCESSIBLE, VIP, or LARGE.");
        }
        slotDao.create(slotCode.trim().toUpperCase(), floor.trim(), normalizedType);
    }

    public void deleteSlot(int slotId) {
        slotDao.delete(slotId);
    }
}
