package com.parking.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

public final class Theme {
    public static final Color BACKGROUND = new Color(245, 247, 250);
    public static final Color SURFACE = Color.WHITE;
    public static final Color PRIMARY = new Color(35, 86, 167);
    public static final Color SUCCESS = new Color(38, 137, 88);
    public static final Color WARNING = new Color(199, 126, 32);
    public static final Color DANGER = new Color(190, 64, 64);
    public static final Color TEXT = new Color(32, 39, 51);
    public static final Color MUTED = new Color(104, 116, 134);
    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 232)),
            BorderFactory.createEmptyBorder(14, 14, 14, 14));

    private Theme() {
    }

    public static JPanel panel() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND);
        return panel;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
}
