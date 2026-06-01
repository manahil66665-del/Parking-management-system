package com.parking;

import com.parking.config.DatabaseInitializer;
import com.parking.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep the default Swing look and feel if the system theme is unavailable.
            }
            new LoginFrame().setVisible(true);
        });
    }
}
