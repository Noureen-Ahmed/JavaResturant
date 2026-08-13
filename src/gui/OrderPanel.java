package gui;

import Dao.FoodItemDAO;
import Dao.InvoiceDAO;
import Dao.OrderDAO;
import Dao.EmployeeDAO;
import Dao.ReservationDAO;
import classes.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
public class OrderPanel extends JPanel {

    private final MainFrame mainFrame;
    private final FoodItemDAO foodItemDao = new FoodItemDAO();
    private final OrderDAO orderDao = new OrderDAO();
    private final InvoiceDAO invoiceDao = new InvoiceDAO();
    private final EmployeeDAO employeeDao = new EmployeeDAO();
    private final ReservationDAO reservationDao = new ReservationDAO();

    private JComboBox<String> foodComboBox;
    private JSpinner quantitySpinner;
    private DefaultTableModel orderTableModel;
    private JLabel subtotalLabel;
    private JLabel totalLabel;
    
    private JComboBox<OrderType> orderTypeCombo;
    private JComboBox<String> deliveryManCombo;
    private JTextField customerNameField;
    private JTextField customerPhoneField;
    private JTextField deliveryAddressField;

    private Order activeOrder = new Order();

    public OrderPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🛒 Create New Order");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);
        add(titleLabel, BorderLayout.NORTH);

        JPanel controlBar = UITheme.createCardPanel();
        controlBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        foodComboBox = new JComboBox<>();
        foodComboBox.setPreferredSize(new Dimension(250, 32));

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        quantitySpinner.setPreferredSize(new Dimension(70, 32));

        JButton addToOrderBtn = UITheme.createButton("Add to Order", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        JButton removeItemBtn = UITheme.createButton("Remove Item", UITheme.COLOR_RED, UITheme.COLOR_CREAM);
        JButton clearOrderBtn = UITheme.createButton("Clear Order", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);

        controlBar.add(new JLabel("Food Item:"));
        controlBar.add(foodComboBox);
        controlBar.add(new JLabel("Qty:"));
        controlBar.add(quantitySpinner);
        controlBar.add(addToOrderBtn);
        controlBar.add(removeItemBtn);
        controlBar.add(clearOrderBtn);

        String[] columns = {"Food Item", "Quantity", "Price", "Total"};
        orderTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable orderTable = UITheme.createStyledTable(orderTableModel);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(controlBar, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = UITheme.createCardPanel();
        bottomPanel.setLayout(new BorderLayout(15, 10));

        JPanel totalsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        totalsPanel.setOpaque(false);

        subtotalLabel = new JLabel("Subtotal: $0.00", SwingConstants.RIGHT);
        subtotalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        subtotalLabel.setForeground(UITheme.COLOR_TEXT);

        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalLabel.setForeground(UITheme.COLOR_DARK_BROWN);

        totalsPanel.add(subtotalLabel);
        totalsPanel.add(totalLabel);

        JButton confirmOrderBtn = UITheme.createButton("Confirm Order & Print Invoice", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        confirmOrderBtn.setFont(new Font("SansSerif", Font.BOLD, 15));

        JPanel orderDetailsPanel = UITheme.createCardPanel();
        orderDetailsPanel.setLayout(new GridLayout(5, 2, 8, 8));
        
        orderTypeCombo = new JComboBox<>(OrderType.values());
        deliveryManCombo = new JComboBox<>();
        customerNameField = new JTextField();
        customerPhoneField = new JTextField();
        deliveryAddressField = new JTextField();

        deliveryManCombo.setEnabled(false);
        customerNameField.setEnabled(false);
        customerPhoneField.setEnabled(false);
        deliveryAddressField.setEnabled(false);
        
        EmployeeDAO empDao = new EmployeeDAO();
        for (Employee e : empDao.getAllEmployees()) {
            if (e.getRole() == Role.DELIVERY) deliveryManCombo.addItem(e.getName());
        }
        
        orderTypeCombo.addActionListener(e -> {
            OrderType type = (OrderType) orderTypeCombo.getSelectedItem();
            boolean isDelivery = (type == OrderType.DELIVERY);
            deliveryManCombo.setEnabled(isDelivery);
            customerNameField.setEnabled(isDelivery);
            customerPhoneField.setEnabled(isDelivery);
            deliveryAddressField.setEnabled(isDelivery);
        });
        
        orderDetailsPanel.add(new JLabel("Order Type:"));
        orderDetailsPanel.add(orderTypeCombo);
        orderDetailsPanel.add(new JLabel("Delivery Driver:"));
        orderDetailsPanel.add(deliveryManCombo);
        orderDetailsPanel.add(new JLabel("Customer Name:"));
        orderDetailsPanel.add(customerNameField);
        orderDetailsPanel.add(new JLabel("Customer Phone:"));
        orderDetailsPanel.add(customerPhoneField);
        orderDetailsPanel.add(new JLabel("Delivery Address:"));
        orderDetailsPanel.add(deliveryAddressField);

        JPanel combineBottom = new JPanel(new BorderLayout(10, 10));
        combineBottom.setOpaque(false);
        combineBottom.add(orderDetailsPanel, BorderLayout.NORTH);
        combineBottom.add(totalsPanel, BorderLayout.CENTER);

        bottomPanel.add(combineBottom, BorderLayout.CENTER);
        bottomPanel.add(confirmOrderBtn, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        addToOrderBtn.addActionListener(e -> addItemToOrder());
        removeItemBtn.addActionListener(e -> removeSelectedItem(orderTable.getSelectedRow()));
        clearOrderBtn.addActionListener(e -> clearOrder());
        confirmOrderBtn.addActionListener(e -> confirmOrder());

        refreshFoodDropdown();
    }

    public void refreshFoodDropdown() {
        foodComboBox.removeAllItems();
        List<FoodItem> items = foodItemDao.getAllFoodItems();
        for (FoodItem item : items) {
            foodComboBox.addItem(item.getId() + " - " + item.getName() + " ($" + item.getPrice() + ")");
        }
    }

    private void addItemToOrder() {
        String selected = (String) foodComboBox.getSelectedItem();
        if (selected == null || selected.isEmpty()) return;

        int foodId = Integer.parseInt(selected.split(" - ")[0]);
        FoodItem item = foodItemDao.getFoodItemById(foodId);
        int qty = (int) quantitySpinner.getValue();

        if (item != null) {
            activeOrder.addItem(item, qty);
            updateOrderTableUI();
        }
    }

    private void removeSelectedItem(int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the order table to remove.");
            return;
        }
        List<OrderItem> items = activeOrder.getItems();
        if (selectedRow < items.size()) {
            activeOrder.removeItem(items.get(selectedRow).getItem());
            updateOrderTableUI();
        }
    }

    private void clearOrder() {
        activeOrder = new Order();
        if (orderTypeCombo != null) orderTypeCombo.setSelectedIndex(0);
        if (customerNameField != null) customerNameField.setText("");
        if (customerPhoneField != null) customerPhoneField.setText("");
        if (deliveryAddressField != null) deliveryAddressField.setText("");
        updateOrderTableUI();
    }

    private void updateOrderTableUI() {
        orderTableModel.setRowCount(0);
        for (OrderItem oi : activeOrder.getItems()) {
            orderTableModel.addRow(new Object[]{
                    oi.getItem().getName(),
                    oi.getQuantity(),
                    String.format("$%.2f", oi.getItem().getPrice()),
                    String.format("$%.2f", oi.getSubtotal())
            });
        }

        double total = activeOrder.calculateTotal();
        subtotalLabel.setText(String.format("Subtotal: $%.2f", total));
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void confirmOrder() {
        if (activeOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your order is empty. Please add food items first.", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        activeOrder.setCashier(mainFrame.getCurrentUser());
        activeOrder.setOrderType((OrderType) orderTypeCombo.getSelectedItem());
        activeOrder.setCustomerName(customerNameField.getText().trim());
        activeOrder.setCustomerPhone(customerPhoneField.getText().trim());

        OrderType type = (OrderType) orderTypeCombo.getSelectedItem();
        if (type == OrderType.DELIVERY) {
            String name = activeOrder.getCustomerName();
            String phone = activeOrder.getCustomerPhone();
            String address = deliveryAddressField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "For delivery orders, Customer Name, Customer Phone and Delivery Address are required.", "Missing Delivery Details", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!phone.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(this, "Customer Phone must be exactly 11 digits.", "Invalid Phone", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (employeeDao.isPhoneExists(phone, -1) || reservationDao.isPhoneExists(phone, -1) || orderDao.isPhoneExists(phone)) {
                JOptionPane.showMessageDialog(this, "Phone number already exists in the system (belongs to an employee, reservation, or existing order).", "Duplicate Phone Number", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String driver = deliveryManCombo.getSelectedItem() != null ? deliveryManCombo.getSelectedItem().toString() : "";
            activeOrder.setDeliveryAddress("Driver: " + driver + " | " + address);
        } else {
            String phone = activeOrder.getCustomerPhone();
            if (phone != null && !phone.isEmpty()) {
                if (!phone.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(this, "Customer Phone must be exactly 11 digits.", "Invalid Phone", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (employeeDao.isPhoneExists(phone, -1) || reservationDao.isPhoneExists(phone, -1) || orderDao.isPhoneExists(phone)) {
                    JOptionPane.showMessageDialog(this, "Phone number already exists in the system.", "Duplicate Phone Number", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        if (orderDao.createOrder(activeOrder)) {
            Invoice invoice = new Invoice(activeOrder.getTotalAmount(), "CASH");
            invoice.setOrder(activeOrder);
            invoice.setPaid(true);
            invoiceDao.createInvoice(invoice);

            InvoiceDialog invDialog = new InvoiceDialog(mainFrame, activeOrder, invoice);
            invDialog.setVisible(true);

            clearOrder();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create order in database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
