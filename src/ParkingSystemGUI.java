import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class ParkingSystemGUI extends JFrame {

    JTextField plateField;
    JComboBox<String> typeBox;
    JTextArea output;

    JPanel slotPanel;
    JButton[] slotButtons;

    Connection con;

    int totalSlots = 10;
    boolean[] slots = new boolean[totalSlots];

    Timer timer;
    LocalDateTime entryTime;

    public ParkingSystemGUI() {

        setTitle("🚗 Parking Management System");
        setSize(700, 600);
        setLayout(new BorderLayout());

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel();
        plateField = new JTextField(10);
        typeBox = new JComboBox<>(new String[]{"Car", "Bike", "Emergency"});

        JButton entryBtn = new JButton("Entry");
        JButton exitBtn = new JButton("Exit");

        topPanel.add(new JLabel("Plate:"));
        topPanel.add(plateField);
        topPanel.add(new JLabel("Type:"));
        topPanel.add(typeBox);
        topPanel.add(entryBtn);
        topPanel.add(exitBtn);

        add(topPanel, BorderLayout.NORTH);

        // ===== SLOT PANEL =====
        slotPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        slotButtons = new JButton[totalSlots];

        for (int i = 0; i < totalSlots; i++) {
            slotButtons[i] = new JButton("Slot " + (i + 1));
            slotButtons[i].setBackground(Color.GREEN);
            slotPanel.add(slotButtons[i]);
        }

        add(slotPanel, BorderLayout.CENTER);

        // ===== OUTPUT PANEL =====
        output = new JTextArea(8, 40);
        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.SOUTH);

        // ===== DB =====
        connectDB();

        // ===== BUTTONS =====
        entryBtn.addActionListener(e -> vehicleEntry());
        exitBtn.addActionListener(e -> vehicleExit());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    // ================= DATABASE =================
    void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/parking_system?useSSL=false&serverTimezone=UTC",
                    "root",
                    "clekxrekx8866"
            );

            output.setText("Database Connected ✅");

        } catch (Exception e) {
            output.setText("Database Connection Failed ❌");
        }
    }

    // ================= SLOT =================
    int getNearestSlot() {
        for (int i = 0; i < totalSlots; i++) {
            if (!slots[i]) {
                slots[i] = true;
                slotButtons[i].setBackground(Color.RED);
                return i + 1;
            }
        }
        return -1;
    }

    void freeSlot(int slot) {
        slots[slot - 1] = false;
        slotButtons[slot - 1].setBackground(Color.GREEN);
    }

    // ================= ENTRY =================
    void vehicleEntry() {

        if (con == null) {
            output.setText("Database not connected!");
            return;
        }

        try {
            String plate = plateField.getText();
            String type = typeBox.getSelectedItem().toString();

            int slot = getNearestSlot();

            if (slot == -1) {
                output.setText("Parking Full ❌");
                return;
            }

            entryTime = LocalDateTime.now();

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO parking (plate_no, vehicle_type, slot_no, entry_time) VALUES (?, ?, ?, NOW())"
            );

            ps.setString(1, plate);
            ps.setString(2, type);
            ps.setInt(3, slot);
            ps.executeUpdate();

            output.setText("Vehicle Parked ✅ Slot: " + slot);

            startTimer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TIMER =================
    void startTimer() {
        timer = new Timer(1000, e -> {

            long sec = ChronoUnit.SECONDS.between(entryTime, LocalDateTime.now());

            long h = sec / 3600;
            long m = (sec % 3600) / 60;
            long s = sec % 60;

            output.setText("Parking Time: " + h + "h " + m + "m " + s + "s");
        });

        timer.start();
    }

    // ================= EXIT =================
    void vehicleExit() {

        if (con == null) {
            output.setText("Database not connected!");
            return;
        }

        try {
            String plate = plateField.getText();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM parking WHERE plate_no=? AND exit_time IS NULL"
            );

            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int slot = rs.getInt("slot_no");
                String type = rs.getString("vehicle_type");
                LocalDateTime entry = rs.getTimestamp("entry_time").toLocalDateTime();
                LocalDateTime exit = LocalDateTime.now();

                long sec = ChronoUnit.SECONDS.between(entry, exit);

                double fee = calculateFee(type, sec);

                PreparedStatement up = con.prepareStatement(
                        "UPDATE parking SET exit_time=NOW(), fee=? WHERE plate_no=? AND exit_time IS NULL"
                );

                up.setDouble(1, fee);
                up.setString(2, plate);
                up.executeUpdate();

                freeSlot(slot);

                if (timer != null) timer.stop();

                generateReceipt(plate, type, slot, entry, exit, fee);

            } else {
                output.setText("Vehicle not found ❌");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FEE =================
    double calculateFee(String type, long sec) {

        double rate = 0;

        if (type.equals("Car")) rate = 100;
        else if (type.equals("Bike")) rate = 50;
        else return 0;

        double hours = Math.max(1, Math.ceil(sec / 3600.0));

        return hours * rate;
    }

    // ================= RECEIPT =================
    void generateReceipt(String plate, String type, int slot,
                         LocalDateTime entry, LocalDateTime exit, double fee) {

        output.setText("====== RECEIPT ======\n");
        output.append("Plate: " + plate + "\n");
        output.append("Type: " + type + "\n");
        output.append("Slot: " + slot + "\n");
        output.append("Entry: " + entry + "\n");
        output.append("Exit: " + exit + "\n");
        output.append("Fee: Rs " + fee + "\n");
    }

    public static void main(String[] args) {
        new ParkingSystemGUI();
    }
}