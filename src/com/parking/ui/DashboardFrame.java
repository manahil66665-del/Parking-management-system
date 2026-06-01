package com.parking.ui;

import com.parking.model.User;
import com.parking.service.AccessControlService;
import com.parking.ui.panels.BillingPanel;
import com.parking.ui.panels.CarInfoPanel;
import com.parking.ui.panels.DashboardPanel;
import com.parking.ui.panels.SlotsPanel;
import com.parking.ui.panels.UserManagementPanel;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class DashboardFrame extends JFrame {
    private final AccessControlService accessControlService = new AccessControlService();

    public DashboardFrame(User user) {
        setTitle("Parking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(Theme.CARD_BORDER);
        header.add(Theme.title("Parking Management System"), BorderLayout.WEST);
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userBox.setBackground(Theme.SURFACE);
        userBox.add(new JLabel(user.getFullName() + " (" + user.getRole().getName() + ")"));
        header.add(userBox, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", new DashboardPanel());
        tabs.addTab("Slots", new SlotsPanel());
        tabs.addTab("Billing", new BillingPanel());
        tabs.addTab("Cars", new CarInfoPanel(user));
        if (accessControlService.isAdmin(user)) {
            tabs.addTab("Users", new UserManagementPanel(user));
        }
        tabs.addChangeListener(event -> refreshSelectedTab((JTabbedPane) event.getSource()));
        add(tabs, BorderLayout.CENTER);
    }

    private void refreshSelectedTab(JTabbedPane tabs) {
        Component selected = tabs.getSelectedComponent();
        if (selected instanceof DashboardPanel) {
            ((DashboardPanel) selected).refresh();
        } else if (selected instanceof SlotsPanel) {
            ((SlotsPanel) selected).refresh();
        } else if (selected instanceof BillingPanel) {
            ((BillingPanel) selected).refresh();
        } else if (selected instanceof CarInfoPanel) {
            ((CarInfoPanel) selected).refreshAll();
        } else if (selected instanceof UserManagementPanel) {
            ((UserManagementPanel) selected).refresh();
        }
    }
}
