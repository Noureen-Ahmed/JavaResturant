package gui;

import Dao.EmployeeDAO;
import classes.Employee;
import classes.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MainFrame extends JFrame {

    private final EmployeeDAO employeeDao = new EmployeeDAO();
    private Employee currentUser = null;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JLabel userInfoLabel;

    private DashboardPanel dashboardPanel;
    private FoodMenuPanel foodMenuPanel;
    private OrderPanel orderPanel;
    private ReservationPanel reservationPanel;
    private AdminPanel adminPanel;
    private DeliveryPanel deliveryPanel;
    private ViewOrdersPanel viewOrdersPanel;

    public MainFrame() {
        setTitle("Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 740);
        setLocationRelativeTo(null);

        if (!showLoginDialog()) {
            System.exit(0);
            return;
        }

        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UITheme.COLOR_BG);

        dashboardPanel = new DashboardPanel(this);
        foodMenuPanel = new FoodMenuPanel();
        orderPanel = new OrderPanel(this);
        reservationPanel = new ReservationPanel();
        adminPanel = new AdminPanel(this);
        deliveryPanel = new DeliveryPanel(this);
        viewOrdersPanel = new ViewOrdersPanel();

        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(foodMenuPanel, "FOOD_MENU");
        mainContentPanel.add(orderPanel, "NEW_ORDER");
        mainContentPanel.add(reservationPanel, "TABLES");
        mainContentPanel.add(adminPanel, "ADMIN");
        mainContentPanel.add(deliveryPanel, "DELIVERY_DASHBOARD");
        mainContentPanel.add(viewOrdersPanel, "VIEW_ORDERS");

        add(mainContentPanel, BorderLayout.CENTER);

        dashboardPanel.updateUserInfo(currentUser);
        if (currentUser.getRole() == Role.DELIVERY) {
            cardLayout.show(mainContentPanel, "DELIVERY_DASHBOARD");
            deliveryPanel.loadOrders();
        } else if (currentUser.getRole() == Role.MANAGER) {
            cardLayout.show(mainContentPanel, "ADMIN");
            adminPanel.loadFoodData();
            adminPanel.loadStaffData();
        } else {
            cardLayout.show(mainContentPanel, "DASHBOARD");
        }
    }

    public void showPanel(String cardName) {
        if ("ADMIN".equals(cardName) && (currentUser == null || currentUser.getRole() != Role.MANAGER)) {
            JOptionPane.showMessageDialog(this, "[ACCESS DENIED] Manager access required for Admin Portal.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("FOOD_MENU".equals(cardName)) {
            foodMenuPanel.loadFoodData();
        } else if ("NEW_ORDER".equals(cardName)) {
            orderPanel.refreshFoodDropdown();
        } else if ("TABLES".equals(cardName)) {
            reservationPanel.loadTablesData();
            reservationPanel.loadReservationsData();
        } else if ("ADMIN".equals(cardName)) {
            adminPanel.loadFoodData();
            adminPanel.loadStaffData();
        } else if ("DELIVERY_DASHBOARD".equals(cardName)) {
            deliveryPanel.loadOrders();
        } else if ("VIEW_ORDERS".equals(cardName)) {
            viewOrdersPanel.loadOrders();
        }

        cardLayout.show(mainContentPanel, cardName);
    }

    private boolean showLoginDialog() {
        JDialog loginDialog = new JDialog((Frame) null, "Login - Restaurant Management", true);
        loginDialog.setSize(420, 320);
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome Back!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setForeground(UITheme.COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(userLabel, gbc);

        JTextField usernameField = new JTextField(15);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        passLabel.setForeground(UITheme.COLOR_TEXT);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(passwordField, gbc);

        JButton loginBtn = UITheme.createButton("Login", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
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
        panel.setBackground(UITheme.COLOR_CREAM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.COLOR_BROWN),
                new EmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("🍔 Restaurant Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_TEXT);
        panel.add(titleLabel, BorderLayout.WEST);

        String userText = "User: " + (currentUser != null ? currentUser.getName() : "Guest") +
                "  |  Role: " + (currentUser != null ? currentUser.getRole() : "N/A");
        userInfoLabel = new JLabel(userText);
        userInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userInfoLabel.setForeground(UITheme.COLOR_TEXT);
        panel.add(userInfoLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.COLOR_BG);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 2, UITheme.COLOR_BROWN),
                new EmptyBorder(20, 15, 20, 15)
        ));

        Dimension btnSize = new Dimension(200, 45);

        JButton dashBtn = createSidebarButton("🏠 Dashboard", btnSize);
        JButton menuBtn = createSidebarButton("🍔 Food Menu", btnSize);
        JButton orderBtn = createSidebarButton("🛒 New Order", btnSize);
        JButton tablesBtn = createSidebarButton("🪑 Tables & Reservations", btnSize);
        JButton adminBtn = createSidebarButton("👨‍💼 Admin / Manager", btnSize);
        JButton logoutBtn = createSidebarButton("🚪 Logout / Exit", btnSize);

        dashBtn.addActionListener(e -> showPanel("DASHBOARD"));
        menuBtn.addActionListener(e -> showPanel("FOOD_MENU"));
        orderBtn.addActionListener(e -> showPanel("NEW_ORDER"));
        tablesBtn.addActionListener(e -> showPanel("TABLES"));
        adminBtn.addActionListener(e -> showPanel("ADMIN"));
        logoutBtn.addActionListener(e -> logout());

        if (currentUser.getRole() == Role.MANAGER) {
            JButton viewOrdersBtn = createSidebarButton("📋 View Orders", btnSize);
            JButton foodMgmtBtn = createSidebarButton("🍔 Food Management", btnSize);
            JButton tableMgmtBtn = createSidebarButton("🪑 Tables Management", btnSize);
            JButton staffMgmtBtn = createSidebarButton("👨‍💼 Staff Management", btnSize);

            viewOrdersBtn.addActionListener(e -> showPanel("VIEW_ORDERS"));
            foodMgmtBtn.addActionListener(e -> { showPanel("ADMIN"); adminPanel.setSelectedTab(0); });
            tableMgmtBtn.addActionListener(e -> { showPanel("ADMIN"); adminPanel.setSelectedTab(1); });
            staffMgmtBtn.addActionListener(e -> { showPanel("ADMIN"); adminPanel.setSelectedTab(2); });

            sidebar.add(viewOrdersBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
            sidebar.add(foodMgmtBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
            sidebar.add(tableMgmtBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
            sidebar.add(staffMgmtBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        } else if (currentUser.getRole() == Role.CASHIER) {
            sidebar.add(dashBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
            sidebar.add(menuBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
            sidebar.add(orderBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
            sidebar.add(tablesBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        } else if (currentUser.getRole() == Role.DELIVERY) {
            JButton myDeliveriesBtn = createSidebarButton("\uD83D\uDE9A My Deliveries", btnSize);
            myDeliveriesBtn.addActionListener(e -> showPanel("DELIVERY_DASHBOARD"));
            sidebar.add(myDeliveriesBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createSidebarButton(String text, Dimension size) {
        JButton btn = UITheme.createButton(text, UITheme.COLOR_CREAM, UITheme.COLOR_TEXT);
        btn.setMaximumSize(size);
        btn.setPreferredSize(size);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        return btn;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
