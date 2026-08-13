package gui;

import Dao.EmployeeDAO;
import Dao.FoodItemDAO;
import Dao.TableDAO;
import Dao.ReservationDAO;
import classes.*;
import exceptions.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {

    private final MainFrame mainFrame;
    private final FoodItemDAO foodItemDao = new FoodItemDAO();
    private final TableDAO tableDao = new TableDAO();
    private final EmployeeDAO employeeDao = new EmployeeDAO();
    private final ReservationDAO reservationDao = new ReservationDAO();

    private JTabbedPane tabbedPane;

    private DefaultTableModel foodTableModel;
    private int selectedFoodId = -1;
    private JTextField foodNameField;
    private JComboBox<String> foodCategoryCombo;
    private JTextField foodPriceField;
    private JButton addFoodBtn;

    private DefaultTableModel staffTableModel;

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("👨‍💼 Admin / Manager Portal");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);
        add(titleLabel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        tabbedPane.addTab("Food Management", createFoodManagementTab());
        tabbedPane.addTab("Tables Management", createTablesManagementTab());
        tabbedPane.addTab("Staff Management", createStaffManagementTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setSelectedTab(int index) {
        if (tabbedPane != null && index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    private JPanel createFoodManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"Food ID", "Food Name", "Category", "Price"};
        foodTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable foodTable = UITheme.createStyledTable(foodTableModel);
        panel.add(new JScrollPane(foodTable), BorderLayout.CENTER);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        foodNameField = new JTextField(15);
        String[] categories = {"Hot Drink", "Cold Drinks", "Coffee", "Main Dish", "Appetizers", "Italian pizza", "milkshakes"};
        foodCategoryCombo = new JComboBox<>(categories);
        foodPriceField = new JTextField(10);

        gbc.gridx=0; gbc.gridy=0; formCard.add(new JLabel("Food Name:"), gbc);
        gbc.gridx=1; formCard.add(foodNameField, gbc);

        gbc.gridx=2; gbc.gridy=0; formCard.add(new JLabel("Category:"), gbc);
        gbc.gridx=3; formCard.add(foodCategoryCombo, gbc);

        gbc.gridx=0; gbc.gridy=1; formCard.add(new JLabel("Price ($):"), gbc);
        gbc.gridx=1; formCard.add(foodPriceField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        JButton addBtn = UITheme.createButton("Add", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        addFoodBtn = addBtn;
        JButton updateBtn = UITheme.createButton("Update", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);
        JButton deleteBtn = UITheme.createButton("Delete", UITheme.COLOR_RED, UITheme.COLOR_CREAM);
        JButton clearBtn = UITheme.createButton("Clear", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=4;
        formCard.add(btnPanel, gbc);

        panel.add(formCard, BorderLayout.SOUTH);

        foodTable.getSelectionModel().addListSelectionListener(e -> {
            int row = foodTable.getSelectedRow();
            if (row != -1) {
                selectedFoodId = Integer.parseInt(foodTableModel.getValueAt(row, 0).toString());
                foodNameField.setText(foodTableModel.getValueAt(row, 1).toString());
                String cat = foodTableModel.getValueAt(row, 2).toString();
                if (cat != null && !cat.isEmpty() && !containsCategory(cat)) {
                    foodCategoryCombo.addItem(cat);
                }
                foodCategoryCombo.setSelectedItem(cat);
                String priceStr = foodTableModel.getValueAt(row, 3).toString().replace("$", "");
                foodPriceField.setText(priceStr);
                addFoodBtn.setEnabled(false);
            } else {
                addFoodBtn.setEnabled(true);
            }
        });

        addBtn.addActionListener(e -> addFoodItem());
        updateBtn.addActionListener(e -> updateFoodItem());
        deleteBtn.addActionListener(e -> deleteFoodItem());
        clearBtn.addActionListener(e -> clearFoodFields());

        loadFoodData();

        return panel;
    }

    public void loadFoodData() {
        foodTableModel.setRowCount(0);
        List<FoodItem> list = foodItemDao.getAllFoodItems();
        for (FoodItem item : list) {
            foodTableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    String.format("$%.2f", item.getPrice())
            });
        }
    }

    private void addFoodItem() {
        try {
            String name = foodNameField.getText().trim();
            String cat = (String) foodCategoryCombo.getSelectedItem();
            double price = Double.parseDouble(foodPriceField.getText().trim());

            if (name.isEmpty() || cat == null || cat.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Food Name and Category cannot be empty.");
                return;
            }

            if (foodItemDao.isNameExists(name, -1)) {
                JOptionPane.showMessageDialog(this, "A food item with this name already exists.\nPlease edit the existing item instead of adding it again.");
                return;
            }

            FoodItem item = new FoodItem(name, price, cat);
            if (foodItemDao.addFoodItem(item)) {
                JOptionPane.showMessageDialog(this, "Food item added successfully!");
                clearFoodFields();
                loadFoodData();
            } else {
                JOptionPane.showMessageDialog(this, "Error adding food item.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price number.");
        }
    }

    private void updateFoodItem() {
        if (selectedFoodId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food item to update.");
            return;
        }
        try {
            int id = selectedFoodId;
            String name = foodNameField.getText().trim();
            String cat = (String) foodCategoryCombo.getSelectedItem();
            double price = Double.parseDouble(foodPriceField.getText().trim());

            if (name.isEmpty() || cat == null || cat.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Food Name and Category cannot be empty.");
                return;
            }

            if (foodItemDao.isNameExists(name, id)) {
                JOptionPane.showMessageDialog(this, "A food item with this name already exists.");
                return;
            }

            FoodItem item = new FoodItem(id, name, price, cat);
            if (foodItemDao.updateFoodItem(item)) {
                JOptionPane.showMessageDialog(this, "Food item updated successfully!");
                clearFoodFields();
                loadFoodData();
            } else {
                JOptionPane.showMessageDialog(this, "Error updating food item.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid input.");
        }
    }

    private void deleteFoodItem() {
        if (selectedFoodId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food item to delete.");
            return;
        }
        int id = selectedFoodId;
        if (JOptionPane.showConfirmDialog(this, "Delete Food Item ID #" + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (foodItemDao.deleteFoodItem(id)) {
                JOptionPane.showMessageDialog(this, "Food item deleted!");
                clearFoodFields();
                loadFoodData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete food item.");
            }
        }
    }

    private void clearFoodFields() {
        selectedFoodId = -1;
        foodNameField.setText("");
        foodCategoryCombo.setSelectedIndex(0);
        foodPriceField.setText("");
        addFoodBtn.setEnabled(true);
    }

    private boolean containsCategory(String cat) {
        for (int i = 0; i < foodCategoryCombo.getItemCount(); i++) {
            if (foodCategoryCombo.getItemAt(i).equals(cat)) return true;
        }
        return false;
    }

    private JPanel createTablesManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"Table Number", "Capacity (Seats)", "Status"};
        DefaultTableModel tModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UITheme.createStyledTable(tModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadTables = () -> {
            tModel.setRowCount(0);
            for (Table t : tableDao.getAllTables()) {
                tModel.addRow(new Object[]{t.getTableNumber(), t.getCapacity(), t.isAvailable() ? "AVAILABLE" : "RESERVED"});
            }
        };
        loadTables.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnPanel.setBackground(UITheme.COLOR_BG);

        JButton addTblBtn = UITheme.createButton("+ Add Table", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        JButton delTblBtn = UITheme.createButton("Delete Table", UITheme.COLOR_RED, UITheme.COLOR_CREAM);

        addTblBtn.addActionListener(e -> {
            String numStr = JOptionPane.showInputDialog(this, "Enter Table Number:");
            String capStr = JOptionPane.showInputDialog(this, "Enter Table Capacity:");
            if (numStr != null && capStr != null) {
                try {
                    int num = Integer.parseInt(numStr.trim());
                    int cap = Integer.parseInt(capStr.trim());
                    if (tableDao.addTable(new Table(num, cap, true))) {
                        JOptionPane.showMessageDialog(this, "Table added!");
                        loadTables.run();
                    }
                } catch (Exception ignored) {}
            }
        });

        delTblBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int tNum = (int) tModel.getValueAt(row, 0);
                if (tableDao.deleteTable(tNum)) {
                    JOptionPane.showMessageDialog(this, "Table deleted!");
                    loadTables.run();
                }
            }
        });

        btnPanel.add(addTblBtn);
        btnPanel.add(delTblBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStaffManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Name", "Phone", "Username", "Role", "Salary"};
        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable staffTable = UITheme.createStyledTable(staffTableModel);
        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnPanel.setBackground(UITheme.COLOR_BG);

        JButton refreshBtn = UITheme.createButton("Refresh Staff", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);
        JButton addEmpBtn = UITheme.createButton("+ Add Employee", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        JButton editEmpBtn = UITheme.createButton("Edit Employee", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);
        JButton delEmpBtn = UITheme.createButton("Delete Employee", UITheme.COLOR_RED, UITheme.COLOR_CREAM);

        refreshBtn.addActionListener(e -> loadStaffData());
        addEmpBtn.addActionListener(e -> openAddEmployeeDialog());
        editEmpBtn.addActionListener(e -> openEditEmployeeDialog(staffTable));
        delEmpBtn.addActionListener(e -> {
            int row = staffTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
                return;
            }
            int empId = (int) staffTableModel.getValueAt(row, 0);
            if (employeeDao.deleteEmployee(empId)) {
                JOptionPane.showMessageDialog(this, "Employee deleted!");
                loadStaffData();
            }
        });

        btnPanel.add(refreshBtn);
        btnPanel.add(addEmpBtn);
        btnPanel.add(editEmpBtn);
        btnPanel.add(delEmpBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        loadStaffData();

        return panel;
    }

    public void loadStaffData() {
        staffTableModel.setRowCount(0);
        for (Employee emp : employeeDao.getAllEmployees()) {
            staffTableModel.addRow(new Object[]{
                    emp.getId(),
                    emp.getName(),
                    emp.getPhone(),
                    emp.getUsername(),
                    emp.getRole(),
                    String.format("$%.2f", emp.getSalary())
            });
        }
    }

    private void openAddEmployeeDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add New Employee", true);
        dialog.setSize(380, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_CREAM);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameF = new JTextField(15);
        JTextField phoneF = new JTextField(15);
        JTextField userF = new JTextField(15);
        JPasswordField passF = new JPasswordField(15);
        JTextField salaryF = new JTextField(15);
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx=1; panel.add(nameF, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx=1; panel.add(phoneF, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx=1; panel.add(userF, gbc);

        gbc.gridx=0; gbc.gridy=3; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx=1; panel.add(passF, gbc);

        gbc.gridx=0; gbc.gridy=4; panel.add(new JLabel("Salary ($):"), gbc);
        gbc.gridx=1; panel.add(salaryF, gbc);

        gbc.gridx=0; gbc.gridy=5; panel.add(new JLabel("Role:"), gbc);
        gbc.gridx=1; panel.add(roleCombo, gbc);

        JButton saveBtn = UITheme.createButton("Save Employee", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        gbc.gridx=0; gbc.gridy=6; gbc.gridwidth=2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            String phone = phoneF.getText().trim();
            String user = userF.getText().trim();
            String pass = new String(passF.getPassword()).trim();
            String salaryStr = salaryF.getText().trim();
            Role role = (Role) roleCombo.getSelectedItem();

            if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.");
                return;
            }

            double salary;
            try {
                salary = salaryStr.isEmpty() ? 0.0 : Double.parseDouble(salaryStr);
                if (salary < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid salary amount.", "Invalid Salary", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                validateEmployeeData(name, phone, user, pass, -1);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Employee emp;
            switch (role) {
                case MANAGER -> emp = new Manager(name, phone, user, pass);
                case CASHIER -> emp = new Cashier(name, phone, user, pass);
                case DELIVERY -> emp = new DeliveryMan(name, phone, user, pass);
                default -> throw new IllegalStateException("Role error: " + role);
            }
            emp.setSalary(salary);

            if (employeeDao.addEmployee(emp)) {
                JOptionPane.showMessageDialog(dialog, "Employee added successfully!");
                dialog.dispose();
                loadStaffData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add employee.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void validateEmployeeData(String name, String phone, String user, String pass, int excludeId) throws Exception {
        if (name.length() < 3 || name.matches(".*\\d.*")) {
            throw new Exception("Name must be at least 3 letters and contain no numbers.");
        }
        if (!phone.matches("\\d{11}")) {
            throw new InvalidPhoneNumberException("Phone number must be exactly 11 digits.");
        }
        if (user.length() < 3) {
            throw new Exception("Username must be at least 3 characters long.");
        }
        if (pass.length() < 8 || !pass.matches(".*[A-Z].*") || !pass.matches(".*[a-z].*") || !pass.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Password must be at least 8 chars long and contain an uppercase letter, a lowercase letter, and a number.");
        }
        if (employeeDao.isPhoneExists(phone, excludeId) || reservationDao.isPhoneExists(phone, -1)) {
            throw new PhoneNumberAlreadyExistsException("Phone number already belongs to someone in the system.");
        }
        if (employeeDao.isUsernameExists(user, excludeId)) {
            throw new UsernameAlreadyExistsException("Username is already taken.");
        }
    }

    private void openEditEmployeeDialog(JTable staffTable) {
        int row = staffTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit.");
            return;
        }
        int empId = (int) staffTableModel.getValueAt(row, 0);
        Employee empToEdit = employeeDao.getEmployeeById(empId);
        if (empToEdit == null) {
            JOptionPane.showMessageDialog(this, "Employee not found.");
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Edit Employee", true);
        dialog.setSize(380, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_CREAM);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameF = new JTextField(empToEdit.getName(), 15);
        JTextField phoneF = new JTextField(empToEdit.getPhone(), 15);
        JTextField userF = new JTextField(empToEdit.getUsername(), 15);
        JPasswordField passF = new JPasswordField(empToEdit.getPassword(), 15);
        JTextField salaryF = new JTextField(String.valueOf(empToEdit.getSalary()), 15);
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        roleCombo.setSelectedItem(empToEdit.getRole());

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx=1; panel.add(nameF, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx=1; panel.add(phoneF, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx=1; panel.add(userF, gbc);

        gbc.gridx=0; gbc.gridy=3; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx=1; panel.add(passF, gbc);

        gbc.gridx=0; gbc.gridy=4; panel.add(new JLabel("Salary ($):"), gbc);
        gbc.gridx=1; panel.add(salaryF, gbc);

        gbc.gridx=0; gbc.gridy=5; panel.add(new JLabel("Role:"), gbc);
        gbc.gridx=1; panel.add(roleCombo, gbc);

        JButton saveBtn = UITheme.createButton("Update Employee", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        gbc.gridx=0; gbc.gridy=6; gbc.gridwidth=2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            String phone = phoneF.getText().trim();
            String user = userF.getText().trim();
            String pass = new String(passF.getPassword()).trim();
            String salaryStr = salaryF.getText().trim();
            Role role = (Role) roleCombo.getSelectedItem();

            if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.");
                return;
            }

            double salary;
            try {
                salary = salaryStr.isEmpty() ? 0.0 : Double.parseDouble(salaryStr);
                if (salary < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid salary amount.", "Invalid Salary", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                validateEmployeeData(name, phone, user, pass, empId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Employee updatedEmp;
            switch (role) {
                case MANAGER -> updatedEmp = new Manager(empId, name, phone, user, pass);
                case CASHIER -> updatedEmp = new Cashier(empId, name, phone, user, pass);
                case DELIVERY -> updatedEmp = new DeliveryMan(empId, name, phone, user, pass);
                default -> throw new IllegalStateException("Role error: " + role);
            }
            updatedEmp.setSalary(salary);

            if (employeeDao.updateEmployee(updatedEmp)) {
                JOptionPane.showMessageDialog(dialog, "Employee updated successfully!");
                dialog.dispose();
                loadStaffData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update employee.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
