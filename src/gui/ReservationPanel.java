package gui;

import Dao.ReservationDAO;
import Dao.TableDAO;
import Dao.EmployeeDAO;
import classes.Reservation;
import classes.Table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ReservationPanel extends JPanel {

    private final TableDAO tableDao = new TableDAO();
    private final ReservationDAO reservationDao = new ReservationDAO();
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    private DefaultTableModel tablesModel;
    private DefaultTableModel reservationsModel;

    public ReservationPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🪑 Tables & Reservations");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);
        add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel tablesPanel = new JPanel(new BorderLayout(15, 15));
        tablesPanel.setBackground(UITheme.COLOR_BG);
        tablesPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] tableCols = {"Table Number", "Capacity (Seats)", "Status"};
        tablesModel = new DefaultTableModel(tableCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablesTable = UITheme.createStyledTable(tablesModel);
        tablesPanel.add(new JScrollPane(tablesTable), BorderLayout.CENTER);

        JPanel tableBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        tableBtns.setBackground(UITheme.COLOR_BG);

        JButton refreshTablesBtn = UITheme.createButton("Refresh Tables", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);
        JButton toggleStatusBtn = UITheme.createButton("Toggle Availability", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);

        refreshTablesBtn.addActionListener(e -> loadTablesData());
        toggleStatusBtn.addActionListener(e -> {
            int row = tablesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a table to toggle status.");
                return;
            }
            int tableNum = (int) tablesModel.getValueAt(row, 0);
            Table t = tableDao.getTableByNumber(tableNum);
            if (t != null) {
                t.setAvailable(!t.isAvailable());
                tableDao.updateTableStatus(t);
                loadTablesData();
            }
        });

        tableBtns.add(refreshTablesBtn);
        tableBtns.add(toggleStatusBtn);
        tablesPanel.add(tableBtns, BorderLayout.SOUTH);

        JPanel resPanel = new JPanel(new BorderLayout(15, 15));
        resPanel.setBackground(UITheme.COLOR_BG);
        resPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] resCols = {"ID", "Customer Name", "Phone", "Date & Time", "Table Number"};
        reservationsModel = new DefaultTableModel(resCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resTable = UITheme.createStyledTable(reservationsModel);
        resPanel.add(new JScrollPane(resTable), BorderLayout.CENTER);

        JPanel resBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        resBtns.setBackground(UITheme.COLOR_BG);

        JButton refreshResBtn = UITheme.createButton("Refresh Reservations", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);
        JButton reserveTableBtn = UITheme.createButton("+ Reserve Table", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        JButton cancelResBtn = UITheme.createButton("Cancel Reservation", UITheme.COLOR_RED, UITheme.COLOR_CREAM);

        refreshResBtn.addActionListener(e -> loadReservationsData());
        reserveTableBtn.addActionListener(e -> openReserveTableDialog());
        cancelResBtn.addActionListener(e -> {
            int row = resTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.");
                return;
            }
            int resId = (int) reservationsModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel Reservation #" + resId + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Reservation r = reservationDao.getReservationById(resId);
                if (reservationDao.deleteReservation(resId)) {
                    if (r != null) {
                        Table t = tableDao.getTableByNumber(r.getTableNumber());
                        if (t != null) {
                            t.setAvailable(true);
                            tableDao.updateTableStatus(t);
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Reservation cancelled successfully.");
                    loadReservationsData();
                    loadTablesData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel reservation.");
                }
            }
        });

        resBtns.add(refreshResBtn);
        resBtns.add(reserveTableBtn);
        resBtns.add(cancelResBtn);
        resPanel.add(resBtns, BorderLayout.SOUTH);

        tabbedPane.addTab("View Tables", tablesPanel);
        tabbedPane.addTab("Reservations", resPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadTablesData();
        loadReservationsData();
    }

    public void loadTablesData() {
        tablesModel.setRowCount(0);
        List<Table> tables = tableDao.getAllTables();
        for (Table t : tables) {
            tablesModel.addRow(new Object[]{
                    t.getTableNumber(),
                    t.getCapacity(),
                    t.isAvailable() ? "AVAILABLE" : "RESERVED / OCCUPIED"
            });
        }
    }

    public void loadReservationsData() {
        reservationsModel.setRowCount(0);
        List<Reservation> list = reservationDao.getAllReservations();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Reservation r : list) {
            reservationsModel.addRow(new Object[]{
                    r.getId(),
                    r.getCustomerName(),
                    r.getPhone(),
                    r.getDate() != null ? sdf.format(r.getDate()) : "N/A",
                    r.getTableNumber()
            });
        }
    }

    private void openReserveTableDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reserve a Table", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_CREAM);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField capacityField = new JTextField(15);
        JComboBox<String> tableCombo = new JComboBox<>();

        List<Table> tables = tableDao.getAllTables();
        for (Table t : tables) tableCombo.addItem(String.valueOf(t.getTableNumber()));

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx=1; panel.add(nameField, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx=1; panel.add(phoneField, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Number of Guests:"), gbc);
        gbc.gridx=1; panel.add(capacityField, gbc);

        gbc.gridx=0; gbc.gridy=3; panel.add(new JLabel("Select Table:"), gbc);
        gbc.gridx=1; panel.add(tableCombo, gbc);

        JButton saveBtn = UITheme.createButton("Confirm Reservation", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String capStr = capacityField.getText().trim();
            String selectedTable = (String) tableCombo.getSelectedItem();

            if (name.isEmpty() || capStr.isEmpty() || selectedTable == null) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.");
                return;
            }
            if (name.length() < 3 || name.matches(".*\\d.*")) {
                JOptionPane.showMessageDialog(dialog, "Name must be at least 3 letters and contain no numbers.");
                return;
            }
            if (!phone.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(dialog, "Phone number must be exactly 11 digits.");
                return;
            }
            if (reservationDao.isPhoneExists(phone, -1) || employeeDao.isPhoneExists(phone, -1)) {
                JOptionPane.showMessageDialog(dialog, "Phone number already belongs to someone in the system.");
                return;
            }

            int guests;
            try {
                guests = Integer.parseInt(capStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number of guests.");
                return;
            }

            int tableNum = Integer.parseInt(selectedTable);
            Table t = tableDao.getTableByNumber(tableNum);
            
            if (!t.isAvailable()) {
                JOptionPane.showMessageDialog(dialog, "Error: Table " + tableNum + " is already reserved!");
                return;
            }
            if (t.getCapacity() < guests) {
                JOptionPane.showMessageDialog(dialog, "Error: Table " + tableNum + " cannot fit " + guests + " guests (Max: " + t.getCapacity() + ").");
                return;
            }

            Reservation res = new Reservation(name, phone, new Date(), tableNum);

            if (reservationDao.addReservation(res)) {
                t.setAvailable(false);
                tableDao.updateTableStatus(t);
                JOptionPane.showMessageDialog(dialog, "Table reserved successfully!");
                dialog.dispose();
                loadReservationsData();
                loadTablesData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to reserve table.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
