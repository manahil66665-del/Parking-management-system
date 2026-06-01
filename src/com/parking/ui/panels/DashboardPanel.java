package com.parking.ui.panels;

import com.parking.service.DashboardService;
import com.parking.ui.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final DashboardService dashboardService = new DashboardService();
    private final JLabel carsValue = metricValue();
    private final JLabel slotsValue = metricValue();
    private final JLabel billsValue = metricValue();
    private final JLabel unpaidValue = metricValue();
    private final SlotChart chart = new SlotChart();

    public DashboardPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 12));
        metrics.setOpaque(false);
        metrics.add(metricCard("Registered Cars", carsValue));
        metrics.add(metricCard("Total Slots", slotsValue));
        metrics.add(metricCard("Invoices", billsValue));
        metrics.add(metricCard("Unpaid Amount", unpaidValue));
        add(metrics, BorderLayout.NORTH);

        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(Theme.SURFACE);
        chartCard.setBorder(Theme.CARD_BORDER);
        chartCard.add(Theme.title("Slot Availability"), BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);
        add(chartCard, BorderLayout.CENTER);

        refresh();
        new Timer(5000, event -> refresh()).start();
    }

    private JPanel metricCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(4, 8));
        card.setBackground(Theme.SURFACE);
        card.setBorder(Theme.CARD_BORDER);
        card.add(Theme.muted(title), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JLabel metricValue() {
        JLabel label = new JLabel("0");
        label.setForeground(Theme.TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 26f));
        return label;
    }

    public void refresh() {
        carsValue.setText(String.valueOf(dashboardService.carCount()));
        slotsValue.setText(String.valueOf(dashboardService.slotCount()));
        billsValue.setText(String.valueOf(dashboardService.billCount()));
        unpaidValue.setText(String.format("Rs. %.2f", dashboardService.unpaidTotal()));
        chart.setData(dashboardService.slotStatusCounts());
    }

    private static class SlotChart extends JPanel {
        private Map<String, Integer> data = Map.of();

        SlotChart() {
            setPreferredSize(new Dimension(500, 360));
            setBackground(Theme.SURFACE);
        }

        void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            int width = getWidth() - 120;
            int baseY = getHeight() - 55;
            int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            int x = 55;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int barHeight = max == 0 ? 0 : (int) ((getHeight() - 120) * (entry.getValue() / (double) max));
                graphics.setColor(colorFor(entry.getKey()));
                graphics.fillRoundRect(x, baseY - barHeight, 74, barHeight, 8, 8);
                graphics.setColor(Theme.TEXT);
                graphics.drawString(String.valueOf(entry.getValue()), x + 30, baseY - barHeight - 8);
                graphics.drawString(entry.getKey(), x - 4, baseY + 22);
                x += Math.max(110, width / Math.max(1, data.size()));
            }
        }

        private Color colorFor(String status) {
            if ("AVAILABLE".equalsIgnoreCase(status)) {
                return Theme.SUCCESS;
            }
            if ("OCCUPIED".equalsIgnoreCase(status)) {
                return Theme.DANGER;
            }
            return Theme.WARNING;
        }
    }
}
