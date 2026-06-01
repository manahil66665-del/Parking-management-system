package com.parking.ui.panels;

import com.parking.model.Car;
import com.parking.model.Slot;
import com.parking.service.CarService;
import com.parking.service.SlotService;
import com.parking.ui.Theme;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class SlotsPanel extends JPanel {
    private final SlotService slotService = new SlotService();
    private final CarService carService = new CarService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Slot", "Floor", "Slot Type", "Status", "Car Type", "Assigned Car" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<CarItem> carCombo = new JComboBox<>();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[] { "ALL", "AVAILABLE", "OCCUPIED" });
    private final JTextField slotCodeField = new JTextField(7);
    private final JTextField floorField = new JTextField("Ground", 8);
    private final JComboBox<String> slotTypeCombo = new JComboBox<>(
            new String[] { "STANDARD", "ACCESSIBLE", "VIP", "LARGE" });
    private final JLabel summaryLabel = Theme.muted("Slots loading...");

    public SlotsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new GridLayout(3, 1, 6, 6));
        top.setOpaque(false);
        JPanel create = new JPanel(new FlowLayout(FlowLayout.LEFT));
        create.setOpaque(false);
        JButton addSlotButton = Theme.primaryButton("Add Slot");
        JButton deleteSlotButton = new JButton("Delete Slot");
        addSlotButton.addActionListener(event -> addSlot());
        deleteSlotButton.addActionListener(event -> deleteSelectedSlot());
        create.add(new JLabel("Slot Code"));
        create.add(slotCodeField);
        create.add(new JLabel("Floor"));
        create.add(floorField);
        create.add(new JLabel("Type"));
        create.add(slotTypeCombo);
        create.add(addSlotButton);
        create.add(deleteSlotButton);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton allocateButton = Theme.primaryButton("Allocate");
        JButton releaseButton = new JButton("Release");
        JButton refreshButton = new JButton("Refresh");
        allocateButton.addActionListener(event -> allocateSelected());
        releaseButton.addActionListener(event -> releaseSelected());
        refreshButton.addActionListener(event -> refresh());
        actions.add(new JLabel("Car"));
        actions.add(carCombo);
        actions.add(allocateButton);
        actions.add(releaseButton);
        actions.add(refreshButton);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setOpaque(false);
        statusFilter.addActionListener(event -> refresh());
        filters.add(new JLabel("Show"));
        filters.add(statusFilter);
        filters.add(summaryLabel);
        top.add(create);
        top.add(actions);
        top.add(filters);
        add(top, BorderLayout.NORTH);

        table.removeColumn(table.getColumnModel().getColumn(0));
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
        new Timer(4000, event -> refresh()).start();
    }

    public void refresh() {
        model.setRowCount(0);
        int available = 0;
        int occupied = 0;
        String selectedStatus = String.valueOf(statusFilter.getSelectedItem());
        for (Slot slot : slotService.allSlots()) {
            if (slot.isAvailable()) {
                available++;
            } else if ("OCCUPIED".equalsIgnoreCase(slot.getStatus())) {
                occupied++;
            }
            if (!"ALL".equals(selectedStatus) && !selectedStatus.equalsIgnoreCase(slot.getStatus())) {
                continue;
            }
            String car = slot.getCar() == null ? "-" : slot.getCar().displayName();
            String carType = slot.getCar() == null ? "-" : slot.getCar().getCarType();
            model.addRow(new Object[] { slot.getId(), slot.getSlotCode(), slot.getFloor(), slot.getType(),
                    slot.getStatus(), carType, car });
        }
        summaryLabel.setText(String.format("Available: %d | Occupied: %d | Total: %d", available, occupied,
                available + occupied));
        carCombo.removeAllItems();
        List<Car> cars = carService.allCars();
        for (Car car : cars) {
            if (!slotService.isCarAssigned(car.getId())) {
                carCombo.addItem(new CarItem(car));
            }
        }
    }

    private void allocateSelected() {
        int row = selectedModelRow();
        CarItem carItem = (CarItem) carCombo.getSelectedItem();
        if (row < 0 || carItem == null) {
            JOptionPane.showMessageDialog(this, "Select a slot and car first.");
            return;
        }
        int slotId = (int) model.getValueAt(row, 0);
        try {
            slotService.allocate(slotId, carItem.car.getId());
            refresh();
            JOptionPane.showMessageDialog(this, "Slot allocated successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Allocation failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void releaseSelected() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a slot first.");
            return;
        }
        int slotId = (int) model.getValueAt(row, 0);
        try {
            slotService.release(slotId);
            refresh();
            JOptionPane.showMessageDialog(this, "Slot released successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Release failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSlot() {
        try {
            slotService.createSlot(slotCodeField.getText(), floorField.getText(),
                    String.valueOf(slotTypeCombo.getSelectedItem()));
            slotCodeField.setText("");
            refresh();
            JOptionPane.showMessageDialog(this, "Slot added successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Slot save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedSlot() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a slot first.");
            return;
        }
        int slotId = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected empty slot?", "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            slotService.deleteSlot(slotId);
            refresh();
            JOptionPane.showMessageDialog(this, "Slot deleted successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Slot delete failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        return viewRow < 0 ? -1 : table.convertRowIndexToModel(viewRow);
    }

    private static class CarItem {
        private final Car car;

        CarItem(Car car) {
            this.car = car;
        }

        @Override
        public String toString() {
            return car.displayName();
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = String.valueOf(value);
                if ("AVAILABLE".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(223, 245, 232));
                } else if ("OCCUPIED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(252, 226, 226));
                } else {
                    component.setBackground(Color.WHITE);
                }
            }
            return component;
        }
    }
}
