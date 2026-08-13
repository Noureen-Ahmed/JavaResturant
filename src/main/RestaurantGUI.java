package main;

import Dao.*;
import classes.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RestaurantGUI extends JFrame {

    private static final Color COLOR_BG = new Color(0xF5, 0xEB, 0xDD);         
    private static final Color COLOR_DARK_BROWN = new Color(0x5C, 0x40, 0x33); 
    private static final Color COLOR_BROWN = new Color(0x8B, 0x5E, 0x3C);     
    private static final Color COLOR_CREAM = new Color(0xFF, 0xF8, 0xEF);      
    private static final Color COLOR_TEXT = new Color(0x3E, 0x27, 0x23);      
    private static final Color COLOR_ACCENT = new Color(0x79, 0x55, 0x48);     

    private final EmployeeDAO employeeDao = new EmployeeDAO();
    private final FoodItemDAO foodItemDao = new FoodItemDAO();
    private final TableDAO tableDao = new TableDAO();
    private final OrderDAO orderDao = new OrderDAO();
    private final InvoiceDAO invoiceDao = new InvoiceDAO();
    private final ReservationDAO reservationDao = new ReservationDAO();

    private Employee currentUser = null;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JLabel userInfoLabel;

    private DefaultTableModel foodTableModel;
    private DefaultTableModel orderBasketModel;
    private DefaultTableModel tablesTableModel;
    private DefaultTableModel reservationsTableModel;
    private DefaultTableModel employeesTableModel;

    private Order currentOrder = new Order();
    private JLabel orderTotalLabel;
    private JComboBox<String> foodSelectCombo;
    private JSpinner quantitySpinner;
    private JComboBox<OrderType> orderTypeCombo;
    private JTextField customerNameField;
    private JTextField customerPhoneField;
    private JTextField deliveryAddressField;
    private JTextField deliveryFeeField;
    private JComboBox<String> tableNumberCombo;

    public RestaurantGUI() {
        setTitle("Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        if (!showLoginDialog()) {
            System.exit(0);
            return;
        }

        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(COLOR_BG);

        mainContentPanel.add(createDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(createFoodMenuPanel(), "FOOD_MENU");
        mainContentPanel.add(createNewOrderPanel(), "NEW_ORDER");
        mainContentPanel.add(createTablesPanel(), "TABLES");
        mainContentPanel.add(createAdminPanel(), "ADMIN");

        add(mainContentPanel, BorderLayout.CENTER);

        cardLayout.show(mainContentPanel, "DASHBOARD");
    }


    private boolean showLoginDialog() {
        JDialog loginDialog = new JDialog((Frame) null, "Login - Restaurant Management", true);
        loginDialog.setSize(420, 320);
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome Back!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_DARK_BROWN);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setForeground(COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(userLabel, gbc);

        JTextField usernameField = new JTextField(15);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        passLabel.setForeground(COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(passwordField, gbc);

        JButton loginBtn = createStyledButton("Login", COLOR_DARK_BROWN, COLOR_CREAM);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(loginBtn, gbc);

        final boolean[] loggedIn = {false};

        loginBtn.addActionListener((ActionEvent e) -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(loginDialog, "Please enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Employee> employees = employeeDao.getAllEmployees();
            for (Employee emp : employees) {
                if (emp.getUsername().equalsIgnoreCase(user) && emp.getPassword().equals(pass)) {
                    currentUser = emp;
                    loggedIn[0] = true;
                    loginDialog.dispose();
                    return;
                }
            }

            JOptionPane.showMessageDialog(loginDialog, "Invalid username or password!", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        });

        loginDialog.add(panel, BorderLayout.CENTER);
        loginDialog.setVisible(true);

        return loggedIn[0];
    }

  

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_CREAM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BROWN),
                new EmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("🍔 Restaurant Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT);
        panel.add(titleLabel, BorderLayout.WEST);

        String userText = "User: " + (currentUser != null ? currentUser.getName() : "Guest") +
                "  |  Role: " + (currentUser != null ? currentUser.getRole() : "N/A");
        userInfoLabel = new JLabel(userText);
        userInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userInfoLabel.setForeground(COLOR_TEXT);
        panel.add(userInfoLabel, BorderLayout.EAST);

        return panel;
    }



    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_BG);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 2, COLOR_BROWN),
                new EmptyBorder(20, 15, 20, 15)
        ));

        Dimension btnSize = new Dimension(200, 45);

        JButton dashBtn = createSidebarButton("🏠 Dashboard", btnSize);
        JButton menuBtn = createSidebarButton("🍔 Food Menu", btnSize);
        JButton orderBtn = createSidebarButton("🛒 New Order", btnSize);
        JButton tablesBtn = createSidebarButton("🪑 Tables & Reservations", btnSize);
        JButton adminBtn = createSidebarButton("👨‍💼 Admin / Manager", btnSize);
        JButton logoutBtn = createSidebarButton("🚪 Logout / Exit", btnSize);

        dashBtn.addActionListener(e -> cardLayout.show(mainContentPanel, "DASHBOARD"));
        menuBtn.addActionListener(e -> {
            loadFoodMenuData();
            cardLayout.show(mainContentPanel, "FOOD_MENU");
        });
        orderBtn.addActionListener(e -> {
            loadOrderFoodItemsCombo();
            loadOrderTablesCombo();
            cardLayout.show(mainContentPanel, "NEW_ORDER");
        });
        tablesBtn.addActionListener(e -> {
            loadTablesData();
            loadReservationsData();
            cardLayout.show(mainContentPanel, "TABLES");
        });
        adminBtn.addActionListener(e -> {
            if (isManager()) {
                loadEmployeesData();
                cardLayout.show(mainContentPanel, "ADMIN");
            } else {
                JOptionPane.showMessageDialog(this, "[ACCESS DENIED] Only Managers can access the Admin Portal!", "Access Denied", JOptionPane.WARNING_MESSAGE);
            }
        });
        logoutBtn.addActionListener(e -> logout());

        sidebar.add(dashBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(menuBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(orderBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(tablesBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(adminBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createSidebarButton(String text, Dimension size) {
        JButton btn = createStyledButton(text, COLOR_DARK_BROWN, COLOR_CREAM);
        btn.setMaximumSize(size);
        btn.setPreferredSize(size);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        return btn;
    }


    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel welcomeCard = createCardPanel();
        welcomeCard.setLayout(new BoxLayout(welcomeCard, BoxLayout.Y_AXIS));
        welcomeCard.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel welcomeLabel = new JLabel("Welcome to Our Restaurant!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        welcomeLabel.setForeground(COLOR_DARK_BROWN);

        JLabel subLabel = new JLabel("Logged in as: " + (currentUser != null ? currentUser.getName() + " (" + currentUser.getRole() + ")" : ""));
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subLabel.setForeground(COLOR_TEXT);

        welcomeCard.add(welcomeLabel);
        welcomeCard.add(Box.createRigidArea(new Dimension(0, 10)));
        welcomeCard.add(subLabel);

        panel.add(welcomeCard, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setBackground(COLOR_BG);
        gridPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        gridPanel.add(createDashboardCard("🍔 Food Menu", "View available food items and menu pricing.", e -> {
            loadFoodMenuData();
            cardLayout.show(mainContentPanel, "FOOD_MENU");
        }));
        gridPanel.add(createDashboardCard("🛒 New Order", "Place new orders and generate customer invoices.", e -> {
            loadOrderFoodItemsCombo();
            loadOrderTablesCombo();
            cardLayout.show(mainContentPanel, "NEW_ORDER");
        }));
        gridPanel.add(createDashboardCard("🪑 Tables & Reservations", "Manage dining table availability and customer bookings.", e -> {
            loadTablesData();
            loadReservationsData();
            cardLayout.show(mainContentPanel, "TABLES");
        }));
        gridPanel.add(createDashboardCard("👨‍💼 Admin / Manager", "Manage staff accounts and system administrative settings.", e -> {
            if (isManager()) {
                loadEmployeesData();
                cardLayout.show(mainContentPanel, "ADMIN");
            } else {
                JOptionPane.showMessageDialog(this, "[ACCESS DENIED] Manager access required.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            }
        }));

        panel.add(gridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDashboardCard(String title, String desc, java.awt.event.ActionListener action) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(COLOR_DARK_BROWN);

        JLabel descLbl = new JLabel("<html><body style='width: 180px;'>" + desc + "</body></html>");
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLbl.setForeground(COLOR_TEXT);

        textPanel.add(titleLbl);
        textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        textPanel.add(descLbl);

        JButton openBtn = createStyledButton("Open", COLOR_BROWN, COLOR_CREAM);
        openBtn.addActionListener(action);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(openBtn, BorderLayout.SOUTH);

        return card;
    }

  

    private JPanel createFoodMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🍔 Food Menu");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_DARK_BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"ID", "Item Name", "Price ($)", "Category"};
        foodTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = createStyledTable(foodTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnPanel.setBackground(COLOR_BG);

        JButton refreshBtn = createStyledButton("Refresh List", COLOR_BROWN, COLOR_CREAM);
        JButton addBtn = createStyledButton("+ Add Food Item", COLOR_DARK_BROWN, COLOR_CREAM);
        JButton deleteBtn = createStyledButton("Delete Selected", new Color(0xA9, 0x32, 0x26), COLOR_CREAM);

        refreshBtn.addActionListener(e -> loadFoodMenuData());
        addBtn.addActionListener(e -> openAddFoodItemDialog());
        deleteBtn.addActionListener(e -> {
            if (!isManager()) {
                JOptionPane.showMessageDialog(this, "Only Managers can delete food items.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a food item to delete.");
                return;
            }
            int id = (int) foodTableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Food Item ID #" + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (foodItemDao.deleteFoodItem(id)) {
                    JOptionPane.showMessageDialog(this, "Food item deleted successfully!");
                    loadFoodMenuData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete food item.");
                }
            }
        });

        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadFoodMenuData() {
        foodTableModel.setRowCount(0);
        List<FoodItem> items = foodItemDao.getAllFoodItems();
        for (FoodItem item : items) {
            foodTableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    String.format("%.2f", item.getPrice()),
                    item.getCategory()
            });
        }
    }

    private void openAddFoodItemDialog() {
        if (!isManager()) {
            JOptionPane.showMessageDialog(this, "Only Managers can add new food items.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Add New Food Item", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_CREAM);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField priceField = new JTextField(15);
        JTextField categoryField = new JTextField(15);

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx=1; panel.add(nameField, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx=1; panel.add(priceField, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Category:"), gbc);
        gbc.gridx=1; panel.add(categoryField, gbc);

        JButton saveBtn = createStyledButton("Save Item", COLOR_DARK_BROWN, COLOR_CREAM);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                String category = categoryField.getText().trim();

                if (name.isEmpty() || category.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                    return;
                }

                FoodItem item = new FoodItem(name, price, category);
                if (foodItemDao.addFoodItem(item)) {
                    JOptionPane.showMessageDialog(dialog, "Food Item added successfully!");
                    dialog.dispose();
                    loadFoodMenuData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Database error adding food item.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid price number.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }


    private JPanel createNewOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🛒 Create New Order & Generate Invoice");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_DARK_BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        mainGrid.setBackground(COLOR_BG);

        JPanel leftCard = createCardPanel();
        leftCard.setLayout(new GridBagLayout());
        leftCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BROWN, 1),
                " Order Details ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16), COLOR_DARK_BROWN
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        foodSelectCombo = new JComboBox<>();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JButton addItemBtn = createStyledButton("+ Add Item to Basket", COLOR_BROWN, COLOR_CREAM);

        orderTypeCombo = new JComboBox<>(OrderType.values());
        customerNameField = new JTextField(15);
        customerPhoneField = new JTextField(15);
        tableNumberCombo = new JComboBox<>();
        deliveryAddressField = new JTextField(15);
        deliveryFeeField = new JTextField("0.0", 10);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Select Food:"), gbc);
        gbc.gridx = 1; leftCard.add(foodSelectCombo, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; leftCard.add(quantitySpinner, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; leftCard.add(addItemBtn, gbc); row++;
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Order Type:"), gbc);
        gbc.gridx = 1; leftCard.add(orderTypeCombo, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1; leftCard.add(customerNameField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Customer Phone:"), gbc);
        gbc.gridx = 1; leftCard.add(customerPhoneField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Table #:"), gbc);
        gbc.gridx = 1; leftCard.add(tableNumberCombo, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Delivery Address:"), gbc);
        gbc.gridx = 1; leftCard.add(deliveryAddressField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; leftCard.add(new JLabel("Delivery Fee ($):"), gbc);
        gbc.gridx = 1; leftCard.add(deliveryFeeField, gbc);

        JPanel rightCard = createCardPanel();
        rightCard.setLayout(new BorderLayout(10, 10));
        rightCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BROWN, 1),
                " Current Order Basket ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16), COLOR_DARK_BROWN
        ));

        String[] cols = {"Item Name", "Price ($)", "Qty", "Subtotal ($)"};
        orderBasketModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable basketTable = createStyledTable(orderBasketModel);
        rightCard.add(new JScrollPane(basketTable), BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new BorderLayout(10, 10));
        checkoutPanel.setBackground(COLOR_CREAM);

        orderTotalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        orderTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        orderTotalLabel.setForeground(COLOR_DARK_BROWN);
        checkoutPanel.add(orderTotalLabel, BorderLayout.NORTH);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionBtns.setOpaque(false);

        JButton removeSelectedBtn = createStyledButton("Remove Item", new Color(0xA9, 0x32, 0x26), COLOR_CREAM);
        JButton checkoutBtn = createStyledButton("Place Order & Print Receipt", COLOR_DARK_BROWN, COLOR_CREAM);
        checkoutBtn.setFont(new Font("SansSerif", Font.BOLD, 14));

        actionBtns.add(removeSelectedBtn);
        actionBtns.add(checkoutBtn);

        checkoutPanel.add(actionBtns, BorderLayout.SOUTH);
        rightCard.add(checkoutPanel, BorderLayout.SOUTH);

        mainGrid.add(leftCard);
        mainGrid.add(rightCard);

        panel.add(mainGrid, BorderLayout.CENTER);

        addItemBtn.addActionListener(e -> {
            String selectedItemStr = (String) foodSelectCombo.getSelectedItem();
            if (selectedItemStr == null || selectedItemStr.isEmpty()) return;

            int itemId = Integer.parseInt(selectedItemStr.split(" - ")[0]);
            FoodItem foodItem = foodItemDao.getFoodItemById(itemId);
            int qty = (int) quantitySpinner.getValue();

            if (foodItem != null) {
                currentOrder.addItem(foodItem, qty);
                updateOrderBasketUI();
            }
        });

        removeSelectedBtn.addActionListener(e -> {
            int selectedRow = basketTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select an item from the basket to remove.");
                return;
            }
            List<OrderItem> items = currentOrder.getItems();
            if (selectedRow < items.size()) {
                currentOrder.removeItem(items.get(selectedRow).getItem());
                updateOrderBasketUI();
            }
        });

        checkoutBtn.addActionListener(e -> submitOrderAndPrintInvoice());

        return panel;
    }

    private void loadOrderFoodItemsCombo() {
        if (foodSelectCombo == null) return;
        foodSelectCombo.removeAllItems();
        List<FoodItem> items = foodItemDao.getAllFoodItems();
        for (FoodItem item : items) {
            foodSelectCombo.addItem(item.getId() + " - " + item.getName() + " ($" + item.getPrice() + ")");
        }
    }

    private void loadOrderTablesCombo() {
        if (tableNumberCombo == null) return;
        tableNumberCombo.removeAllItems();
        tableNumberCombo.addItem("None");
        List<Table> tables = tableDao.getAllTables();
        for (Table t : tables) {
            tableNumberCombo.addItem(String.valueOf(t.getTableNumber()));
        }
    }

    private void updateOrderBasketUI() {
        orderBasketModel.setRowCount(0);
        for (OrderItem oi : currentOrder.getItems()) {
            orderBasketModel.addRow(new Object[]{
                    oi.getItem().getName(),
                    String.format("%.2f", oi.getItem().getPrice()),
                    oi.getQuantity(),
                    String.format("%.2f", oi.getSubtotal())
            });
        }

        double deliveryFee = 0.0;
        try {
            deliveryFee = Double.parseDouble(deliveryFeeField.getText().trim());
        } catch (Exception ignored) {}
        currentOrder.setDeliveryFee(deliveryFee);

        orderTotalLabel.setText(String.format("Total: $%.2f", currentOrder.calculateTotal()));
    }

    private void submitOrderAndPrintInvoice() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The order basket is empty!", "Order Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        OrderType selectedType = (OrderType) orderTypeCombo.getSelectedItem();
        currentOrder.setOrderType(selectedType != null ? selectedType : OrderType.DINE_IN);
        currentOrder.setCashier(currentUser);
        currentOrder.setCustomerName(customerNameField.getText().trim());
        currentOrder.setCustomerPhone(customerPhoneField.getText().trim());
        currentOrder.setDeliveryAddress(deliveryAddressField.getText().trim());

        try {
            String selectedTable = (String) tableNumberCombo.getSelectedItem();
            if (selectedTable != null && !selectedTable.equals("None")) {
                currentOrder.setTableNumber(Integer.parseInt(selectedTable));
            } else {
                currentOrder.setTableNumber(0);
            }
        } catch (Exception ignored) {}

        if (orderDao.createOrder(currentOrder)) {
            Invoice invoice = new Invoice(currentOrder.getTotalAmount(), "CASH");
            invoice.setOrder(currentOrder);
            invoice.setPaid(true);
            invoiceDao.createInvoice(invoice);

            showInvoiceReceiptDialog(currentOrder, invoice);

            currentOrder = new Order();
            updateOrderBasketUI();
            customerNameField.setText("");
            customerPhoneField.setText("");
            deliveryAddressField.setText("");
            deliveryFeeField.setText("0.0");
        } else {
            JOptionPane.showMessageDialog(this, "Database error creating order.", "Order Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showInvoiceReceiptDialog(Order order, Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        sb.append("=========================================\n");
        sb.append("       RESTAURANT RECEIPT & INVOICE      \n");
        sb.append("=========================================\n");
        sb.append("Invoice ID   : #").append(invoice.getInvoiceId()).append("\n");
        sb.append("Order ID     : #").append(order.getOrderId()).append("\n");
        sb.append("Date         : ").append(sdf.format(order.getOrderDate())).append("\n");
        sb.append("Order Type   : ").append(order.getOrderType()).append("\n");
        sb.append("Cashier      : ").append(currentUser != null ? currentUser.getName() : "N/A").append("\n");
        if (order.getCustomerName() != null && !order.getCustomerName().isEmpty()) {
            sb.append("Customer     : ").append(order.getCustomerName()).append("\n");
        }
        if (order.getTableNumber() > 0) {
            sb.append("Table        : #").append(order.getTableNumber()).append("\n");
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-20s %-5s %-10s\n", "Item", "Qty", "Subtotal"));
        sb.append("-----------------------------------------\n");

        for (OrderItem oi : order.getItems()) {
            sb.append(String.format("%-20s %-5d $%-10.2f\n", oi.getItem().getName(), oi.getQuantity(), oi.getSubtotal()));
        }

        if (order.getOrderType() == OrderType.DELIVERY) {
            sb.append(String.format("%-20s %-5s $%-10.2f\n", "Delivery Fee", "-", order.getDeliveryFee()));
        }

        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT : $%.2f\n", order.getTotalAmount()));
        sb.append("Payment Method: ").append(invoice.getPaymentMethod()).append("\n");
        sb.append("Status       : PAID\n");
        sb.append("=========================================\n");
        sb.append("     Thank you for your visit!           \n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBackground(COLOR_CREAM);

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Order Invoice Receipt", JOptionPane.INFORMATION_MESSAGE);
    }


    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🪑 Restaurant Tables & Reservations");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_DARK_BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel tablesTab = new JPanel(new BorderLayout(15, 15));
        tablesTab.setBackground(COLOR_BG);
        tablesTab.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] tableCols = {"Table Number", "Capacity (Seats)", "Status"};
        tablesTableModel = new DefaultTableModel(tableCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablesTable = createStyledTable(tablesTableModel);
        tablesTab.add(new JScrollPane(tablesTable), BorderLayout.CENTER);

        JPanel tableBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        tableBtnBar.setBackground(COLOR_BG);

        JButton refreshTablesBtn = createStyledButton("Refresh", COLOR_BROWN, COLOR_CREAM);
        JButton addTableBtn = createStyledButton("+ Add Table", COLOR_DARK_BROWN, COLOR_CREAM);
        JButton toggleStatusBtn = createStyledButton("Toggle Availability", COLOR_DARK_BROWN, COLOR_CREAM);

        refreshTablesBtn.addActionListener(e -> loadTablesData());
        addTableBtn.addActionListener(e -> openAddTableDialog());
        toggleStatusBtn.addActionListener(e -> {
            int row = tablesTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a table to toggle availability.");
                return;
            }
            int tableNum = (int) tablesTableModel.getValueAt(row, 0);
            Table t = tableDao.getTableByNumber(tableNum);
            if (t != null) {
                t.setAvailable(!t.isAvailable());
                tableDao.updateTableStatus(t);
                loadTablesData();
            }
        });

        tableBtnBar.add(refreshTablesBtn);
        tableBtnBar.add(addTableBtn);
        tableBtnBar.add(toggleStatusBtn);
        tablesTab.add(tableBtnBar, BorderLayout.SOUTH);

        JPanel resTab = new JPanel(new BorderLayout(15, 15));
        resTab.setBackground(COLOR_BG);
        resTab.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] resCols = {"ID", "Customer Name", "Phone", "Date & Time", "Table #"};
        reservationsTableModel = new DefaultTableModel(resCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resTable = createStyledTable(reservationsTableModel);
        resTab.add(new JScrollPane(resTable), BorderLayout.CENTER);

        JPanel resBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        resBtnBar.setBackground(COLOR_BG);

        JButton refreshResBtn = createStyledButton("Refresh", COLOR_BROWN, COLOR_CREAM);
        JButton addResBtn = createStyledButton("+ New Reservation", COLOR_DARK_BROWN, COLOR_CREAM);

        refreshResBtn.addActionListener(e -> loadReservationsData());
        addResBtn.addActionListener(e -> openAddReservationDialog());

        resBtnBar.add(refreshResBtn);
        resBtnBar.add(addResBtn);
        resTab.add(resBtnBar, BorderLayout.SOUTH);

        tabbedPane.addTab("Dining Tables", tablesTab);
        tabbedPane.addTab("Reservations", resTab);

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadTablesData() {
        tablesTableModel.setRowCount(0);
        List<Table> list = tableDao.getAllTables();
        for (Table t : list) {
            tablesTableModel.addRow(new Object[]{
                    t.getTableNumber(),
                    t.getCapacity(),
                    t.isAvailable() ? "AVAILABLE" : "OCCUPIED / RESERVED"
            });
        }
    }

    private void loadReservationsData() {
        reservationsTableModel.setRowCount(0);
        List<Reservation> list = reservationDao.getAllReservations();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Reservation r : list) {
            reservationsTableModel.addRow(new Object[]{
                    r.getId(),
                    r.getCustomerName(),
                    r.getPhone(),
                    r.getDate() != null ? sdf.format(r.getDate()) : "N/A",
                    r.getTableNumber()
            });
        }
    }

    private void openAddTableDialog() {
        if (!isManager()) {
            JOptionPane.showMessageDialog(this, "Only Managers can add tables.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Add New Table", true);
        dialog.setSize(350, 230);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_CREAM);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField numField = new JTextField(15);
        JTextField capField = new JTextField("4", 15);

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Table Number:"), gbc);
        gbc.gridx=1; panel.add(numField, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Capacity (Seats):"), gbc);
        gbc.gridx=1; panel.add(capField, gbc);

        JButton saveBtn = createStyledButton("Add Table", COLOR_DARK_BROWN, COLOR_CREAM);
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                int tNum = Integer.parseInt(numField.getText().trim());
                int cap = Integer.parseInt(capField.getText().trim());
                Table t = new Table(tNum, cap, true);
                if (tableDao.addTable(t)) {
                    JOptionPane.showMessageDialog(dialog, "Table added successfully!");
                    dialog.dispose();
                    loadTablesData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error adding table (Table Number may already exist).");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void openAddReservationDialog() {
        JDialog dialog = new JDialog(this, "New Table Reservation", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_CREAM);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JComboBox<String> tableCombo = new JComboBox<>();

        List<Table> tables = tableDao.getAllTables();
        for (Table t : tables) tableCombo.addItem(String.valueOf(t.getTableNumber()));

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx=1; panel.add(nameField, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx=1; panel.add(phoneField, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Table Number:"), gbc);
        gbc.gridx=1; panel.add(tableCombo, gbc);

        JButton saveBtn = createStyledButton("Save Reservation", COLOR_DARK_BROWN, COLOR_CREAM);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String selectedTable = (String) tableCombo.getSelectedItem();

            if (name.isEmpty() || selectedTable == null) {
                JOptionPane.showMessageDialog(dialog, "Customer name and table are required.");
                return;
            }

            int tNum = Integer.parseInt(selectedTable);
            Reservation res = new Reservation(name, phone, new Date(), tNum);

            if (reservationDao.addReservation(res)) {
                JOptionPane.showMessageDialog(dialog, "Reservation created successfully!");
                dialog.dispose();
                loadReservationsData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Database error adding reservation.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }



    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("👨‍💼 Admin / Manager Portal - Staff Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_DARK_BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Phone", "Username", "Role", "Available"};
        employeesTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = createStyledTable(employeesTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnPanel.setBackground(COLOR_BG);

        JButton refreshBtn = createStyledButton("Refresh", COLOR_BROWN, COLOR_CREAM);
        JButton addEmpBtn = createStyledButton("+ Add New Employee", COLOR_DARK_BROWN, COLOR_CREAM);
        JButton deleteEmpBtn = createStyledButton("Delete Employee", new Color(0xA9, 0x32, 0x26), COLOR_CREAM);

        refreshBtn.addActionListener(e -> loadEmployeesData());
        addEmpBtn.addActionListener(e -> openAddEmployeeDialog());
        deleteEmpBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an employee to delete.");
                return;
            }
            int empId = (int) employeesTableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete employee ID #" + empId + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (employeeDao.deleteEmployee(empId)) {
                    JOptionPane.showMessageDialog(this, "Employee deleted.");
                    loadEmployeesData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete employee.");
                }
            }
        });

        btnPanel.add(refreshBtn);
        btnPanel.add(addEmpBtn);
        btnPanel.add(deleteEmpBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadEmployeesData() {
        employeesTableModel.setRowCount(0);
        List<Employee> list = employeeDao.getAllEmployees();
        for (Employee emp : list) {
            boolean avail = (emp instanceof DeliveryMan) ? ((DeliveryMan) emp).isAvailable() : true;
            employeesTableModel.addRow(new Object[]{
                    emp.getId(),
                    emp.getName(),
                    emp.getPhone(),
                    emp.getUsername(),
                    emp.getRole(),
                    avail ? "YES" : "NO"
            });
        }
    }

    private void openAddEmployeeDialog() {
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setSize(400, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_CREAM);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx=1; panel.add(nameField, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx=1; panel.add(phoneField, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx=1; panel.add(userField, gbc);

        gbc.gridx=0; gbc.gridy=3; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx=1; panel.add(passField, gbc);

        gbc.gridx=0; gbc.gridy=4; panel.add(new JLabel("Role:"), gbc);
        gbc.gridx=1; panel.add(roleCombo, gbc);

        JButton saveBtn = createStyledButton("Save Employee", COLOR_DARK_BROWN, COLOR_CREAM);
        gbc.gridx=0; gbc.gridy=5; gbc.gridwidth=2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            Role role = (Role) roleCombo.getSelectedItem();

            if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.");
                return;
            }

            Employee emp;
            switch (role) {
                case MANAGER -> emp = new Manager(name, phone, user, pass);
                case CASHIER -> emp = new Cashier(name, phone, user, pass);
                case DELIVERY -> emp = new DeliveryMan(name, phone, user, pass);
                default -> throw new IllegalStateException("Unexpected role: " + role);
            }

            if (employeeDao.addEmployee(emp)) {
                JOptionPane.showMessageDialog(dialog, "Employee added successfully!");
                dialog.dispose();
                loadEmployeesData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error adding employee (Username may already exist).");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

   

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CREAM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BROWN, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_CREAM);
        btn.setForeground(COLOR_TEXT); 
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BROWN, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(COLOR_TEXT);
        
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(COLOR_CREAM);
        table.getTableHeader().setForeground(COLOR_TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        
        table.setSelectionBackground(COLOR_BG);
        table.setSelectionForeground(COLOR_TEXT);
        table.setGridColor(COLOR_BROWN);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setForeground(COLOR_TEXT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    private boolean isManager() {
        return currentUser != null && currentUser.getRole() == Role.MANAGER;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new RestaurantGUI().setVisible(true));
        }
    }

  

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            RestaurantGUI gui = new RestaurantGUI();
            gui.setVisible(true);
        });
    }
}
