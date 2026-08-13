package gui;

import classes.Invoice;
import classes.Order;
import classes.OrderItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;

public class InvoiceDialog extends JDialog {

    public InvoiceDialog(Frame parent, Order order, Invoice invoice) {
        super(parent, "Order Invoice", true);
        setSize(500, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(UITheme.COLOR_CREAM);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel restName = new JLabel("RESTAURANT MANAGEMENT SYSTEM", SwingConstants.CENTER);
        restName.setFont(new Font("SansSerif", Font.BOLD, 18));
        restName.setForeground(UITheme.COLOR_DARK_BROWN);
        restName.setAlignmentX(Component.CENTER_ALIGNMENT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = order.getOrderDate() != null ? sdf.format(order.getOrderDate()) : "";

        JLabel invMeta = new JLabel("Invoice ID: #" + invoice.getInvoiceId() + "   |   Date: " + dateStr, SwingConstants.CENTER);
        invMeta.setFont(new Font("SansSerif", Font.PLAIN, 13));
        invMeta.setForeground(UITheme.COLOR_TEXT);
        invMeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(restName);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(invMeta);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Food Item", "Quantity", "Price", "Total"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (OrderItem oi : order.getItems()) {
            tableModel.addRow(new Object[]{
                    oi.getItem().getName(),
                    oi.getQuantity(),
                    String.format("$%.2f", oi.getItem().getPrice()),
                    String.format("$%.2f", oi.getSubtotal())
            });
        }

        JTable table = UITheme.createStyledTable(tableModel);
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setOpaque(false);

        JLabel totalLabel = new JLabel(String.format("TOTAL: $%.2f", order.getTotalAmount()), SwingConstants.RIGHT);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        totalLabel.setForeground(UITheme.COLOR_DARK_BROWN);

        JButton printBtn = UITheme.createButton("🖨️ Print Invoice", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        printBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Invoice printed successfully!", "Print Invoice", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        footerPanel.add(totalLabel, BorderLayout.NORTH);
        footerPanel.add(printBtn, BorderLayout.SOUTH);

        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }
}
