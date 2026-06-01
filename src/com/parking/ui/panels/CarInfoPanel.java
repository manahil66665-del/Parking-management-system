package com.parking.ui.panels;

import com.parking.model.Car;
import com.parking.model.User;
import com.parking.service.AccessControlService;
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
import java.util.Optional;

public class CarInfoPanel extends JPanel {
    private final CarService carService = new CarService();
    private final AccessControlService accessControlService = new AccessControlService();
    private final User currentUser;
    private final JTextField searchField = new JTextField(12);
    private final JTextField regField = new JTextField(10);
    private final JTextField ownerField = new JTextField(12);
    private final JTextField modelField = new JTextField(12);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[] { "SEDAN", "JEEP", "TRUCK", "OTHER" });
    private final JTextField colorField = new JTextField(8);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] { "ID", "Registration", "Owner", "Model", "Type", "Color", "Status" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public CarInfoPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel north = new JPanel(new GridLayout(2, 1, 6, 6));
        north.setOpaque(false);
        north.add(searchBar());
        north.add(createBar());
        add(north, BorderLayout.NORTH);

        table.removeColumn(table.getColumnModel().getColumn(0));
        add(new JScrollPane(table), BorderLayout.CENTER);
        refreshAll();
    }

    private JPanel searchBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JButton searchButton = Theme.primaryButton("Find Car");
        JButton allButton = new JButton("Show All");
        searchButton.addActionListener(event -> search());
        allButton.addActionListener(event -> refreshAll());
        panel.add(new JLabel("Registration"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(allButton);
        return panel;
    }

    private JPanel createBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JButton addButton = new JButton("Add Car");
        JButton deleteButton = new JButton("Delete Car");
        addButton.addActionListener(event -> addCar());
        deleteButton.addActionListener(event -> deleteSelectedCar());
        panel.add(new JLabel("Reg"));
        panel.add(regField);
        panel.add(new JLabel("Owner"));
        panel.add(ownerField);
        panel.add(new JLabel("Model"));
        panel.add(modelField);
        panel.add(new JLabel("Type"));
        panel.add(typeCombo);
        panel.add(new JLabel("Color"));
        panel.add(colorField);
        panel.add(addButton);
        if (accessControlService.isAdmin(currentUser)) {
            panel.add(deleteButton);
        }
        return panel;
    }

    private void search() {
        try {
            Optional<Car> car = carService.findByRegistration(searchField.getText());
            tableModel.setRowCount(0);
            if (car.isPresent()) {
                addCarRow(car.get());
            } else {
                JOptionPane.showMessageDialog(this, "No car found with this registration.");
            }
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Search failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCar() {
        try {
            carService.createCar(regField.getText(), ownerField.getText(), modelField.getText(),
                    String.valueOf(typeCombo.getSelectedItem()), colorField.getText());
            regField.setText("");
            ownerField.setText("");
            modelField.setText("");
            typeCombo.setSelectedItem("SEDAN");
            colorField.setText("");
            refreshAll();
            JOptionPane.showMessageDialog(this, "Car saved successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Car save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCar() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a car first.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int carId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected car and its related bills/bookings?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            carService.deleteCar(carId);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Car deleted successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Car delete failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshAll() {
        tableModel.setRowCount(0);
        for (Car car : carService.allCars()) {
            addCarRow(car);
        }
    }

    private void addCarRow(Car car) {
        tableModel.addRow(new Object[] { car.getId(), car.getRegistrationNo(), car.getOwnerName(), car.getModel(),
                car.getCarType(), car.getColor(), car.getStatus() });
    }
}
