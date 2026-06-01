package com.parking.ui.panels;

import com.parking.model.User;
import com.parking.service.UserService;
import com.parking.ui.Theme;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class UserManagementPanel extends JPanel {
    private final UserService userService = new UserService();
    private final User currentUser;
    private final JTextField usernameField = new JTextField(10);
    private final JTextField fullNameField = new JTextField(14);
    private final JPasswordField passwordField = new JPasswordField(10);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[] { "STAFF", "ADMIN" });
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Username", "Full Name", "Role", "Active" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public UserManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel north = new JPanel(new GridLayout(2, 1, 6, 6));
        north.setOpaque(false);
        north.add(createBar());
        north.add(actionBar());
        add(north, BorderLayout.NORTH);

        table.removeColumn(table.getColumnModel().getColumn(0));
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private JPanel createBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JButton createButton = Theme.primaryButton("Create User");
        JButton updateButton = new JButton("Update User");
        JButton clearButton = new JButton("Clear");
        createButton.addActionListener(event -> createUser());
        updateButton.addActionListener(event -> updateUser());
        clearButton.addActionListener(event -> clearForm());
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Full Name"));
        panel.add(fullNameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        panel.add(new JLabel("Role"));
        panel.add(roleCombo);
        panel.add(createButton);
        panel.add(updateButton);
        panel.add(clearButton);
        return panel;
    }

    private JPanel actionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JButton activateButton = new JButton("Activate");
        JButton deactivateButton = new JButton("Deactivate");
        JButton deleteButton = new JButton("Delete User");
        JButton refreshButton = new JButton("Refresh");
        activateButton.addActionListener(event -> setSelectedActive(true));
        deactivateButton.addActionListener(event -> setSelectedActive(false));
        deleteButton.addActionListener(event -> deleteSelectedUser());
        refreshButton.addActionListener(event -> refresh());
        panel.add(activateButton);
        panel.add(deactivateButton);
        panel.add(deleteButton);
        panel.add(refreshButton);
        return panel;
    }

    private void createUser() {
        try {
            userService.createUser(usernameField.getText(), new String(passwordField.getPassword()),
                    fullNameField.getText(), String.valueOf(roleCombo.getSelectedItem()));
            usernameField.setText("");
            fullNameField.setText("");
            passwordField.setText("");
            refresh();
            JOptionPane.showMessageDialog(this, "User created successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "User save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        Integer userId = selectedUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        try {
            userService.updateUser(userId, usernameField.getText(), new String(passwordField.getPassword()),
                    fullNameField.getText(), String.valueOf(roleCombo.getSelectedItem()), currentUser);
            passwordField.setText("");
            refresh();
            JOptionPane.showMessageDialog(this, "User updated successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "User update failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setSelectedActive(boolean active) {
        Integer userId = selectedUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        try {
            userService.setActive(userId, active, currentUser);
            refresh();
            JOptionPane.showMessageDialog(this, active ? "User activated." : "User deactivated.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "User update failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        Integer userId = selectedUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected user?", "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            userService.deleteUser(userId, currentUser);
            refresh();
            JOptionPane.showMessageDialog(this, "User deleted successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "User delete failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer selectedUserId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int row = table.convertRowIndexToModel(viewRow);
        return (Integer) model.getValueAt(row, 0);
    }

    public void refresh() {
        model.setRowCount(0);
        for (User user : userService.allUsers()) {
            model.addRow(new Object[] { user.getId(), user.getUsername(), user.getFullName(),
                    user.getRole().getName(), user.isActive() ? "Yes" : "No" });
        }
    }

    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        usernameField.setText(String.valueOf(model.getValueAt(row, 1)));
        fullNameField.setText(String.valueOf(model.getValueAt(row, 2)));
        roleCombo.setSelectedItem(String.valueOf(model.getValueAt(row, 3)));
        passwordField.setText("");
    }

    private void clearForm() {
        table.clearSelection();
        usernameField.setText("");
        fullNameField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedItem("STAFF");
    }
}
