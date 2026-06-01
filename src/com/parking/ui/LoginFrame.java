package com.parking.ui;

import com.parking.model.User;
import com.parking.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Parking Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel outer = Theme.panel();
        outer.setLayout(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(220, 225, 232)),
                BorderFactory.createEmptyBorder(24, 26, 24, 26)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(Theme.title("Parking Management"), gbc);

        gbc.gridy++;
        card.add(Theme.muted("Desktop login with role-based access"), gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        card.add(usernameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        card.add(passwordField, gbc);

        JButton loginButton = Theme.primaryButton("Login");
        loginButton.addActionListener(event -> doLogin());
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        card.add(loginButton, gbc);

        outer.add(card);
        return outer;
    }

    private void doLogin() {
        try {
            User user = authService.login(usernameField.getText(), new String(passwordField.getPassword()));
            new DashboardFrame(user).setVisible(true);
            dispose();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
