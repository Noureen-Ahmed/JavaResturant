package gui;

import Dao.OrderDAO;
import classes.Order;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;


public class ViewOrdersPanel extends JPanel {

    private final OrderDAO orderDao = new OrderDAO();
    private DefaultTableModel ordersModel;
    private JLabel totalLabel;

    public ViewOrdersPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("📋 View Orders");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);
        add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Type", "Date", "Status", "Total ($)"};
        ordersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UITheme.createStyledTable(ordersModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        totalLabel = new JLabel("Total Orders: 0   |   Total Amount: $0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalLabel.setForeground(UITheme.COLOR_DARK_BROWN);

        JPanel footer = UITheme.createCardPanel();
        footer.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.add(totalLabel);
        add(footer, BorderLayout.SOUTH);

        loadOrders();
    }

    public void loadOrders() {
        ordersModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Order> orders = orderDao.getAllOrders();

        double grandTotal = 0.0;
        for (Order o : orders) {
            grandTotal += o.getTotalAmount();
            ordersModel.addRow(new Object[]{
                    o.getOrderId(),
                    o.getOrderType(),
                    o.getOrderDate() != null ? sdf.format(o.getOrderDate()) : "",
                    o.getStatus(),
                    String.format("%.2f", o.getTotalAmount())
            });
        }

        totalLabel.setText("Total Orders: " + orders.size()
                + "   |   Total Amount: $" + String.format("%.2f", grandTotal));
    }
}