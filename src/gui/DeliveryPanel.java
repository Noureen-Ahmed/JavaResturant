package gui;

import Dao.OrderDAO;
import classes.Employee;
import classes.Order;
import classes.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryPanel extends JPanel {

    private final MainFrame mainFrame;
    private final OrderDAO orderDao = new OrderDAO();
    private DefaultTableModel ordersModel;
    private JTable ordersTable;
    private final List<Integer> orderIdByRow = new ArrayList<>();

    public DeliveryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🚚 My Assigned Deliveries");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);
        add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"Order #", "Date", "Customer Name", "Customer Phone", "Delivery Address", "Total ($)", "Status"};
        ordersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ordersTable = UITheme.createStyledTable(ordersModel);
        add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        JButton refreshBtn = UITheme.createButton("Refresh", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);
        JButton completeBtn = UITheme.createButton("Mark Completed", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        
        refreshBtn.addActionListener(e -> loadOrders());
        completeBtn.addActionListener(e -> completeOrder());

        bottomPanel.add(refreshBtn);
        bottomPanel.add(completeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadOrders() {
        ordersModel.setRowCount(0);
        orderIdByRow.clear();
        Employee currentUser = mainFrame.getCurrentUser();
        if (currentUser == null) return;

        List<Order> deliveries = orderDao.getDeliveriesByDriverName(currentUser.getName());
        for (Order o : deliveries) {
            orderIdByRow.add(o.getOrderId());
            ordersModel.addRow(new Object[]{
                o.getOrderId(),
                o.getOrderDate(),
                o.getCustomerName() != null ? o.getCustomerName() : "",
                o.getCustomerPhone() != null ? o.getCustomerPhone() : "",
                cleanAddress(o.getDeliveryAddress()),
                String.format("$%.2f", o.getTotalAmount()),
                o.getStatus()
            });
        }
    }

    private String cleanAddress(String raw) {
        if (raw == null) return "";
        if (raw.contains(" | ")) {
            return raw.substring(raw.indexOf(" | ") + 3);
        }
        return raw;
    }
    
    private void completeOrder() {
        int row = ordersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an order to mark completed.");
            return;
        }
        int displayNumber = (int) ordersModel.getValueAt(row, 0);
        int orderId = orderIdByRow.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Mark Order #" + displayNumber + " as completed? This will remove it from the system.", 
            "Confirm Completion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (orderDao.deleteOrder(orderId)) {
                JOptionPane.showMessageDialog(this, "Order #" + displayNumber + " completed and removed from the system!");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to complete order.");
            }
        }
    }
}
