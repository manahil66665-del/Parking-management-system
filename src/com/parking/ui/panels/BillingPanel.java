package com.parking.ui.panels;

import com.parking.model.Bill;
import com.parking.model.Car;
import com.parking.service.BillingService;
import com.parking.service.CarService;
import com.parking.ui.Theme;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class BillingPanel extends JPanel {
    private final BillingService billingService = new BillingService();
    private final CarService carService = new CarService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Invoice", "Registration", "Owner", "Type", "Amount", "Status", "Issued", "Paid",
                    "Notes" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<CarItem> carCombo = new JComboBox<>();
    private final JTextField amountField = new JTextField(8);
    private final JTextField notesField = new JTextField(18);
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[] { "ALL", "UNPAID", "PAID" });
    private final JLabel summaryLabel = Theme.muted("No invoices yet.");

    public BillingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel north = new JPanel(new GridLayout(2, 1, 6, 6));
        north.setOpaque(false);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setOpaque(false);
        JButton createButton = Theme.primaryButton("Create Invoice");
        JButton paidButton = new JButton("Mark Paid");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        createButton.addActionListener(event -> createBill());
        paidButton.addActionListener(event -> markPaid());
        deleteButton.addActionListener(event -> deleteBill());
        refreshButton.addActionListener(event -> refresh());
        statusFilter.addActionListener(event -> refresh());
        form.add(new JLabel("Car"));
        form.add(carCombo);
        form.add(new JLabel("Amount"));
        form.add(amountField);
        form.add(new JLabel("Notes"));
        form.add(notesField);
        form.add(createButton);
        form.add(paidButton);
        form.add(deleteButton);
        form.add(refreshButton);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setOpaque(false);
        filters.add(new JLabel("Status"));
        filters.add(statusFilter);
        filters.add(summaryLabel);
        north.add(form);
        north.add(filters);
        add(north, BorderLayout.NORTH);

        table.removeColumn(table.getColumnModel().getColumn(0));
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        carCombo.removeAllItems();
        for (Car car : carService.allCars()) {
            carCombo.addItem(new CarItem(car));
        }

        model.setRowCount(0);
        List<Bill> bills = billingService.allBills();
        double paidTotal = 0;
        double unpaidTotal = 0;
        String selectedStatus = String.valueOf(statusFilter.getSelectedItem());
        for (Bill bill : bills) {
            if ("PAID".equalsIgnoreCase(bill.getStatus())) {
                paidTotal += bill.getAmount();
            } else {
                unpaidTotal += bill.getAmount();
            }
            if (!"ALL".equals(selectedStatus) && !selectedStatus.equalsIgnoreCase(bill.getStatus())) {
                continue;
            }
            model.addRow(new Object[] { bill.getId(), bill.getInvoiceNo(), bill.getCar().getRegistrationNo(),
                    bill.getCar().getOwnerName(), bill.getCar().getCarType(), String.format("%.2f", bill.getAmount()),
                    bill.getStatus(), bill.getIssuedAt(), bill.getPaidAt() == null ? "-" : bill.getPaidAt(),
                    bill.getNotes() == null ? "" : bill.getNotes() });
        }
        summaryLabel.setText(String.format("Invoices: %d | Paid: Rs. %.2f | Unpaid: Rs. %.2f", bills.size(),
                paidTotal, unpaidTotal));
    }

    private void createBill() {
        CarItem carItem = (CarItem) carCombo.getSelectedItem();
        if (carItem == null) {
            JOptionPane.showMessageDialog(this, "Select a car first.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            billingService.createBill(carItem.car.getId(), amount, notesField.getText().trim());
            amountField.setText("");
            notesField.setText("");
            refresh();
            JOptionPane.showMessageDialog(this, "Invoice created successfully.");
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Validation",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Billing failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markPaid() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill first.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int billId = (int) model.getValueAt(row, 0);
        try {
            billingService.markPaid(billId);
            refresh();
            JOptionPane.showMessageDialog(this, "Invoice marked as paid.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Billing failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBill() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill first.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int billId = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected bill?", "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                billingService.deleteBill(billId);
                refresh();
                JOptionPane.showMessageDialog(this, "Invoice deleted successfully.");
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Billing failed", JOptionPane.ERROR_MESSAGE);
            }
        }
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
}
