import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

interface ParkingOperations {
    void parkVehicle();
    void removeVehicle();
    void displayVehicles();
    void updateSlotAvailability();
}

public class ParkingManagementSystem extends JFrame implements ParkingOperations {
    private static final long serialVersionUID = 1L;

    // Register JDBC Driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    // Database Configuration - Update DB_PASSWORD with your MySQL root password.
    private static final String URL =
            "jdbc:mysql://localhost:3306/smart_parking?serverTimezone=Asia/Kolkata";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";

    // Form Input Fields
    private final JTextField number = new JTextField();
    private final JTextField owner = new JTextField();
    private final JTextField slot = new JTextField();
    private final JComboBox<String> type =
            new JComboBox<>(new String[]{"Car", "Bike", "Scooter", "Truck"});

    // Records Table Model
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Record ID", "Parking Slot", "Vehicle Number",
                    "Owner Name", "Vehicle Type", "Entry Time", "Status"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    // Tabbed Pane for Navigation
    private final JTabbedPane tabbedPane = new JTabbedPane();

    // Stats Labels for Tab 1 (Banner)
    private final JLabel totalBannerLabel = new JLabel("🅿️ Total: 20", JLabel.CENTER);
    private final JLabel availBannerLabel = new JLabel("🟢 Available: --", JLabel.CENTER);
    private final JLabel occBannerLabel = new JLabel("🔴 Occupied: --", JLabel.CENTER);

    // Stats Labels for Tab 2 (Dashboard Cards)
    private final JLabel totalCardCount = new JLabel("20", JLabel.CENTER);
    private final JLabel availCardCount = new JLabel("--", JLabel.CENTER);
    private final JLabel occCardCount = new JLabel("--", JLabel.CENTER);

    // Slot Grid Components
    private final JPanel slotsGridPanel = new JPanel(new GridLayout(4, 5, 12, 12));
    private final JComboBox<String> filterCombo =
            new JComboBox<>(new String[]{"All Slots (20)", "Available Only", "Occupied Only"});

    // Cached list of slots for filtering
    private final List<ParkingSlot> currentSlots = new ArrayList<>();

    public ParkingManagementSystem() {
        setTitle("Smart Parking Management System");
        setSize(1120, 740);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header Title
        JLabel heading = new JLabel("SMART PARKING MANAGEMENT SYSTEM", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Build Tabs
        JPanel tab1 = createVehicleOperationsTab();
        JPanel tab2 = createSlotAvailabilityTab();

        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.addTab("🚗 Vehicle Entry & Exit", tab1);
        tabbedPane.addTab("🅿️ Slot Availability Dashboard", tab2);

        // Main Frame Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        // Hide Record ID column from UI table, but keep it in the model for checkout logic.
        table.removeColumn(table.getColumnModel().getColumn(0));

        // Initial Data Load
        displayVehicles();
        updateSlotAvailability();

        setVisible(true);
    }

    /**
     * Tab 1: Vehicle Entry, Exit, and Records Table
     */
    private JPanel createVehicleOperationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Quick Stats Banner at Top of Tab 1
        JPanel statsBanner = new JPanel(new GridLayout(1, 3, 10, 5));
        statsBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        statsBanner.setBackground(new Color(245, 247, 250));

        Font bannerFont = new Font("Arial", Font.BOLD, 14);
        totalBannerLabel.setFont(bannerFont);
        totalBannerLabel.setForeground(new Color(41, 128, 185));

        availBannerLabel.setFont(bannerFont);
        availBannerLabel.setForeground(new Color(39, 174, 96));

        occBannerLabel.setFont(bannerFont);
        occBannerLabel.setForeground(new Color(192, 57, 43));

        statsBanner.add(totalBannerLabel);
        statsBanner.add(availBannerLabel);
        statsBanner.add(occBannerLabel);

        // Input Form
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Vehicle Entry Details"));
        form.add(new JLabel("Vehicle Number (e.g. DL01AD1001):"));
        form.add(number);
        form.add(new JLabel("Owner Name:"));
        form.add(owner);
        form.add(new JLabel("Vehicle Type:"));
        form.add(type);
        form.add(new JLabel("Parking Slot (P01 to P20):"));
        form.add(slot);

        // Control Buttons
        JButton parkBtn = new JButton("Park Vehicle (Entry)");
        JButton removeBtn = new JButton("Remove Vehicle (Exit)");
        JButton displayBtn = new JButton("Refresh Records");
        JButton clearBtn = new JButton("Clear Form");
        JButton viewSlotsBtn = new JButton("View Slot Grid ➔");

        parkBtn.setFont(new Font("Arial", Font.BOLD, 12));
        removeBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewSlotsBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewSlotsBtn.setBackground(new Color(230, 242, 255));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(parkBtn);
        buttons.add(removeBtn);
        buttons.add(displayBtn);
        buttons.add(clearBtn);
        buttons.add(viewSlotsBtn);

        // Center section: Form + Table
        JPanel northSection = new JPanel(new BorderLayout(5, 5));
        northSection.add(statsBanner, BorderLayout.NORTH);
        northSection.add(form, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(northSection, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        // Action Listeners
        parkBtn.addActionListener(e -> parkVehicle());
        removeBtn.addActionListener(e -> removeVehicle());
        displayBtn.addActionListener(e -> {
            displayVehicles();
            updateSlotAvailability();
        });
        clearBtn.addActionListener(e -> clearFields());
        viewSlotsBtn.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        return panel;
    }

    /**
     * Tab 2: Visual Slot Availability Dashboard (Module Core)
     */
    private JPanel createSlotAvailabilityTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Dashboard Summary Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));

        cardsPanel.add(createCard("Total Slots", totalCardCount, new Color(52, 152, 219)));
        cardsPanel.add(createCard("Available Slots", availCardCount, new Color(46, 204, 113)));
        cardsPanel.add(createCard("Occupied Slots", occCardCount, new Color(231, 76, 60)));

        // Filter and Action Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterBar.add(new JLabel("Filter View:"));
        filterBar.add(filterCombo);

        JButton refreshBtn = new JButton("Refresh Slots");
        refreshBtn.addActionListener(e -> updateSlotAvailability());
        filterBar.add(refreshBtn);

        JLabel hint = new JLabel("  * Click any GREEN available slot to assign it to the entry form.");
        hint.setFont(new Font("Arial", Font.ITALIC, 12));
        hint.setForeground(Color.DARK_GRAY);
        filterBar.add(hint);

        filterCombo.addActionListener(e -> filterSlotsGrid());

        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.add(cardsPanel, BorderLayout.NORTH);
        topContainer.add(filterBar, BorderLayout.SOUTH);

        // Center Grid
        slotsGridPanel.setBackground(new Color(248, 249, 250));
        JScrollPane scrollPane = new JScrollPane(slotsGridPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Parking Slots Layout (P01 - P20)"));

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Helper to create attractive metric cards
     */
    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private static String activePassword = DB_PASSWORD;
    private static boolean promptAttempted = false;

    private Connection getConnection() throws SQLException {
        // If placeholder is unchanged, prompt interactively once for convenience
        if ("YOUR_MYSQL_PASSWORD".equals(activePassword) && !promptAttempted) {
            promptAttempted = true;
            String envPass = System.getenv("MYSQL_PASSWORD");
            if (envPass != null && !envPass.trim().isEmpty()) {
                activePassword = envPass.trim();
            } else {
                JPasswordField pf = new JPasswordField(15);
                JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
                panel.add(new JLabel("Enter your MySQL root password to connect:"));
                panel.add(pf);
                int result = JOptionPane.showConfirmDialog(this, panel,
                        "MySQL Database Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    activePassword = new String(pf.getPassword());
                }
            }
        }
        return DriverManager.getConnection(URL, DB_USER, activePassword);
    }

    /**
     * Auto-setup and table verification to prevent runtime errors.
     */
    private void ensureDatabaseSetup(Connection con) {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS parking_slots (" +
                    "slot_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "slot_number VARCHAR(10) NOT NULL UNIQUE, " +
                    "slot_type VARCHAR(20) NOT NULL DEFAULT 'Standard', " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'Available', " +
                    "current_vehicle_number VARCHAR(20) NULL)";
            try (Statement st = con.createStatement()) {
                st.executeUpdate(createTable);
            }

            // Ensure 20 slots (P01 to P20) exist
            String countSql = "SELECT COUNT(*) FROM parking_slots";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
                if (rs.next() && rs.getInt(1) < 20) {
                    String insert = "INSERT IGNORE INTO parking_slots (slot_number, slot_type, status) VALUES (?, 'Standard', 'Available')";
                    try (PreparedStatement ps = con.prepareStatement(insert)) {
                        for (int i = 1; i <= 20; i++) {
                            ps.setString(1, String.format("P%02d", i));
                            ps.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Database auto-setup notice: " + ex.getMessage());
        }
    }

    /**
     * Check if a slot number is within valid format (P01 to P20).
     */
    private boolean isValidSlotNumber(String slotNumber) {
        if (slotNumber == null) return false;
        String s = slotNumber.trim().toUpperCase();
        if (!s.matches("^P(0[1-9]|1[0-9]|20)$")) {
            return false;
        }
        return true;
    }

    /**
     * PARK VEHICLE (ENTRY):
     * Validates input, prevents duplicates, checks availability, inserts record, updates slot.
     */
    @Override
    public void parkVehicle() {
        String vehicleNumber = number.getText().trim().toUpperCase();
        String ownerName = owner.getText().trim();
        String parkingSlot = slot.getText().trim().toUpperCase();
        String vehicleType = String.valueOf(type.getSelectedItem());

        // 1. Validate Empty Fields
        if (vehicleNumber.isEmpty() || ownerName.isEmpty() || parkingSlot.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields (Vehicle Number, Owner Name, Vehicle Type, Parking Slot).",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Validate Slot Range (P01 to P20)
        if (!isValidSlotNumber(parkingSlot)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid parking slot '" + parkingSlot + "'!\nPlease select a valid slot between P01 and P20.",
                    "Invalid Slot Assignment", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // OOP Concept: Create Vehicle object via Polymorphic Factory
        Vehicle vehicle = Vehicle.create(vehicleNumber, ownerName, vehicleType);

        try (Connection con = getConnection()) {
            ensureDatabaseSetup(con);

            // 3. Validation: Prevent Duplicate Active Vehicle Entry
            String dupCheckSql = "SELECT slot_number FROM parking_records WHERE vehicle_number = ? AND status = 'Parked'";
            try (PreparedStatement dupCheck = con.prepareStatement(dupCheckSql)) {
                dupCheck.setString(1, vehicle.getVehicleNumber());
                try (ResultSet rs = dupCheck.executeQuery()) {
                    if (rs.next()) {
                        String activeSlot = rs.getString("slot_number");
                        JOptionPane.showMessageDialog(this,
                                "Duplicate Entry Denied!\nVehicle " + vehicle.getVehicleNumber() +
                                        " is already parked in slot " + activeSlot + ".\nA vehicle cannot enter twice without checking out first.",
                                "Duplicate Vehicle", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 4. Validation: Check if Slot is Already Occupied
            String slotCheckSql = "SELECT vehicle_number FROM parking_records WHERE slot_number = ? AND status = 'Parked'";
            try (PreparedStatement slotCheck = con.prepareStatement(slotCheckSql)) {
                slotCheck.setString(1, parkingSlot);
                try (ResultSet rs = slotCheck.executeQuery()) {
                    if (rs.next()) {
                        String occupyingVeh = rs.getString("vehicle_number");
                        JOptionPane.showMessageDialog(this,
                                "Slot " + parkingSlot + " is already OCCUPIED by vehicle " + occupyingVeh + "!\nPlease choose another available slot.",
                                "Slot Occupied", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 5. Insert Entry into parking_records
            String insertSql = "INSERT INTO parking_records " +
                    "(vehicle_number, owner_name, vehicle_type, slot_number, entry_time, status) " +
                    "VALUES (?, ?, ?, ?, ?, 'Parked')";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setString(1, vehicle.getVehicleNumber());
                ps.setString(2, vehicle.getOwnerName());
                ps.setString(3, vehicle.getVehicleType());
                ps.setString(4, parkingSlot);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            // 6. Update parking_slots master table
            String updateSlotSql = "UPDATE parking_slots SET status = 'Occupied', current_vehicle_number = ? WHERE slot_number = ?";
            try (PreparedStatement psSlot = con.prepareStatement(updateSlotSql)) {
                psSlot.setString(1, vehicle.getVehicleNumber());
                psSlot.setString(2, parkingSlot);
                psSlot.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Vehicle parked successfully in slot " + parkingSlot + "!\nRate: ₹" +
                            vehicle.getHourlyRate() + " / hour",
                    "Entry Confirmed", JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            displayVehicles();
            updateSlotAvailability();

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /**
     * REMOVE VEHICLE (EXIT):
     * Calculates duration & fee, updates records to Completed, frees up slot.
     */
    @Override
    public void removeVehicle() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an active parked vehicle from the table first.",
                    "No Vehicle Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int recordId = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        String slotNumber = model.getValueAt(modelRow, 1).toString();
        String vehicleNumber = model.getValueAt(modelRow, 2).toString();
        String ownerName = model.getValueAt(modelRow, 3).toString();
        String vehicleType = model.getValueAt(modelRow, 4).toString();
        String currentStatus = model.getValueAt(modelRow, 6).toString();

        if (!"Parked".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "This record is already 'Completed'. Only currently parked vehicles can be removed.",
                    "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // OOP Concept: Use Polymorphic Vehicle rate calculation
        Vehicle vehicle = Vehicle.create(vehicleNumber, ownerName, vehicleType);
        double hourlyRate = vehicle.getHourlyRate();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        // Fee calculation: round duration, guarantee minimum fee
        String updateRecordSql = "UPDATE parking_records SET exit_time = ?, " +
                "duration_hours = ROUND(GREATEST(TIMESTAMPDIFF(MINUTE, entry_time, ?) / 60.0, 0.05), 2), " +
                "parking_fee = ROUND(GREATEST(TIMESTAMPDIFF(MINUTE, entry_time, ?) / 60.0, 1.0) * ?, 2), " +
                "status = 'Completed' WHERE record_id = ? AND status = 'Parked'";

        try (Connection con = getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(updateRecordSql)) {
                ps.setTimestamp(1, now);
                ps.setTimestamp(2, now);
                ps.setTimestamp(3, now);
                ps.setDouble(4, hourlyRate);
                ps.setInt(5, recordId);
                int updated = ps.executeUpdate();

                if (updated > 0) {
                    // Update parking_slots master table: Vacate slot
                    String freeSlotSql = "UPDATE parking_slots SET status = 'Available', current_vehicle_number = NULL WHERE slot_number = ?";
                    try (PreparedStatement psFree = con.prepareStatement(freeSlotSql)) {
                        psFree.setString(1, slotNumber);
                        psFree.executeUpdate();
                    }

                    // Retrieve calculated fee for confirmation message
                    String feeQuery = "SELECT duration_hours, parking_fee FROM parking_records WHERE record_id = ?";
                    try (PreparedStatement psFee = con.prepareStatement(feeQuery)) {
                        psFee.setInt(1, recordId);
                        try (ResultSet rs = psFee.executeQuery()) {
                            if (rs.next()) {
                                double hours = rs.getDouble("duration_hours");
                                double fee = rs.getDouble("parking_fee");
                                JOptionPane.showMessageDialog(this,
                                        "Vehicle " + vehicleNumber + " Checked Out Successfully!\n" +
                                                "Slot " + slotNumber + " is now AVAILABLE.\n\n" +
                                                "Duration: " + hours + " hrs\n" +
                                                "Rate: ₹" + hourlyRate + " / hr\n" +
                                                "Total Fee: ₹" + fee,
                                        "Exit & Receipt Summary", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Record could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            displayVehicles();
            updateSlotAvailability();

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /**
     * DISPLAY VEHICLES:
     * Populates records table ordered by latest first.
     */
    @Override
    public void displayVehicles() {
        model.setRowCount(0);
        String sql = "SELECT record_id, slot_number, vehicle_number, owner_name, " +
                "vehicle_type, entry_time, status FROM parking_records ORDER BY record_id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("record_id"),
                        rs.getString("slot_number"),
                        rs.getString("vehicle_number"),
                        rs.getString("owner_name") == null ? "" : rs.getString("owner_name"),
                        rs.getString("vehicle_type"),
                        rs.getTimestamp("entry_time"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /**
     * SLOT AVAILABILITY MODULE:
     * Fetches current status for all 20 slots (P01 to P20),
     * updates counters, and renders color-coded visual cards.
     */
    @Override
    public void updateSlotAvailability() {
        currentSlots.clear();
        int occupiedCount = 0;

        try (Connection con = getConnection()) {
            ensureDatabaseSetup(con);

            // Cross-reference slots with active parked records for real-time accuracy
            String sql = "SELECT s.slot_number, s.slot_type, " +
                    "CASE WHEN r.record_id IS NOT NULL THEN 'Occupied' ELSE 'Available' END AS live_status, " +
                    "r.vehicle_number " +
                    "FROM parking_slots s " +
                    "LEFT JOIN parking_records r ON s.slot_number = r.slot_number AND r.status = 'Parked' " +
                    "ORDER BY s.slot_number ASC";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String slotNum = rs.getString("slot_number");
                    String slotType = rs.getString("slot_type");
                    String status = rs.getString("live_status");
                    String vehicleNum = rs.getString("vehicle_number");
                    boolean isOccupied = "Occupied".equalsIgnoreCase(status);

                    if (isOccupied) {
                        occupiedCount++;
                    }

                    // OOP Concept: Encapsulating data into ParkingSlot object
                    ParkingSlot psObj = new ParkingSlot(slotNum, slotType, isOccupied, vehicleNum);
                    currentSlots.add(psObj);
                }
            }
        } catch (SQLException ex) {
            // Keep app functional even before DB password is set
            System.err.println("Notice: Could not query slots from database: " + ex.getMessage());
        }

        // Fallback default 20 slots if database returned empty
        if (currentSlots.isEmpty()) {
            for (int i = 1; i <= 20; i++) {
                currentSlots.add(new ParkingSlot(String.format("P%02d", i)));
            }
        }

        int totalCount = currentSlots.size();
        int availableCount = totalCount - occupiedCount;

        // Update Tab 1 Banner Labels
        totalBannerLabel.setText("🅿️ Total: " + totalCount);
        availBannerLabel.setText("🟢 Available: " + availableCount);
        occBannerLabel.setText("🔴 Occupied: " + occupiedCount);

        // Update Tab 2 Card Labels
        totalCardCount.setText(String.valueOf(totalCount));
        availCardCount.setText(String.valueOf(availableCount));
        occCardCount.setText(String.valueOf(occupiedCount));

        // Re-render visual grid
        filterSlotsGrid();
    }

    /**
     * Filters and renders the visual grid based on user selection.
     */
    private void filterSlotsGrid() {
        slotsGridPanel.removeAll();
        int filterIndex = filterCombo.getSelectedIndex(); // 0 = All, 1 = Available, 2 = Occupied

        for (ParkingSlot s : currentSlots) {
            if (filterIndex == 1 && s.isOccupied()) continue; // Skip occupied
            if (filterIndex == 2 && !s.isOccupied()) continue; // Skip available

            JButton slotBtn = new JButton();
            slotBtn.setLayout(new BorderLayout(2, 2));
            slotBtn.setFocusPainted(false);
            slotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel numLabel = new JLabel(s.getSlotNumber(), JLabel.CENTER);
            numLabel.setFont(new Font("Arial", Font.BOLD, 18));
            numLabel.setForeground(Color.WHITE);

            JLabel statusLabel = new JLabel(s.getStatus().toUpperCase(), JLabel.CENTER);
            statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
            statusLabel.setForeground(Color.WHITE);

            JLabel subLabel;

            if (s.isOccupied()) {
                // RED for OCCUPIED
                slotBtn.setBackground(new Color(220, 53, 69));
                slotBtn.setBorder(BorderFactory.createLineBorder(new Color(180, 40, 55), 2, true));
                subLabel = new JLabel("🚗 " + s.getCurrentVehicleNumber(), JLabel.CENTER);
                subLabel.setForeground(new Color(255, 230, 230));
                slotBtn.setToolTipText("Slot " + s.getSlotNumber() + " is OCCUPIED by " + s.getCurrentVehicleNumber());
            } else {
                // GREEN for AVAILABLE
                slotBtn.setBackground(new Color(40, 167, 69));
                slotBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 130, 55), 2, true));
                subLabel = new JLabel("✔ Click to Select", JLabel.CENTER);
                subLabel.setForeground(new Color(230, 255, 230));
                slotBtn.setToolTipText("Slot " + s.getSlotNumber() + " is AVAILABLE. Click to select.");
            }

            subLabel.setFont(new Font("Arial", Font.PLAIN, 11));

            slotBtn.add(numLabel, BorderLayout.NORTH);
            slotBtn.add(statusLabel, BorderLayout.CENTER);
            slotBtn.add(subLabel, BorderLayout.SOUTH);

            // Interactivity: Clicking slot
            slotBtn.addActionListener(e -> {
                if (!s.isOccupied()) {
                    slot.setText(s.getSlotNumber());
                    tabbedPane.setSelectedIndex(0); // Switch to Entry Form
                    JOptionPane.showMessageDialog(this,
                            "Slot " + s.getSlotNumber() + " selected!\nEnter vehicle details and click 'Park Vehicle'.",
                            "Slot Selected", JOptionPane.INFORMATION_MESSAGE);
                    number.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Slot: " + s.getSlotNumber() + "\n" +
                                    "Status: OCCUPIED\n" +
                                    "Vehicle Number: " + s.getCurrentVehicleNumber() + "\n\n" +
                                    "To check out this vehicle, select it in the records table and click 'Remove Vehicle'.",
                            "Slot Details", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            slotsGridPanel.add(slotBtn);
        }

        slotsGridPanel.revalidate();
        slotsGridPanel.repaint();
    }

    private void clearFields() {
        number.setText("");
        owner.setText("");
        slot.setText("");
        type.setSelectedIndex(0);
    }

    private void showDatabaseError(SQLException ex) {
        String msg = "Database error:\n" + ex.getMessage();
        if (ex.getErrorCode() == 1045) {
            promptAttempted = false; // Reset so user can re-enter password on next try
            activePassword = "YOUR_MYSQL_PASSWORD";
            msg += "\n\nTip: Access denied (Error 1045). Please check your MySQL root password.\nYou can also set DB_PASSWORD directly in ParkingManagementSystem.java.";
        } else if (ex.getErrorCode() == 1049) {
            msg += "\n\nTip: Database 'smart_parking' does not exist yet (Error 1049).\nPlease open and execute database_setup.sql in MySQL Workbench.";
        } else {
            msg += "\n\nCheck that MySQL Server is running on port 3306 and database 'smart_parking' exists.";
        }
        JOptionPane.showMessageDialog(this, msg, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    public static void main(String[] args) {
        // Run Swing UI on Event Dispatch Thread
        SwingUtilities.invokeLater(ParkingManagementSystem::new);
    }
}
