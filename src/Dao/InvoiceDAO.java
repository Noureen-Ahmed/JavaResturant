package Dao;

import classes.Invoice;
import classes.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    public boolean createInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoices (order_id, amount, payment_method, payment_date, is_paid) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (invoice.getOrder() != null) {
                pstmt.setInt(1, invoice.getOrder().getOrderId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            pstmt.setDouble(2, invoice.getAmount());
            pstmt.setString(3, invoice.getPaymentMethod());
            
            Timestamp paymentTimestamp = invoice.getPaymentDate() != null 
                    ? new Timestamp(invoice.getPaymentDate().getTime()) 
                    : new Timestamp(System.currentTimeMillis());
            pstmt.setTimestamp(4, paymentTimestamp);
            
            pstmt.setBoolean(5, invoice.isPaid());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoice.setInvoiceId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating invoice: " + e.getMessage());
        }
        return false;
    }

    public Invoice getInvoiceById(int invoiceId) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoice: " + e.getMessage());
        }
        return null;
    }

    public Invoice getInvoiceByOrderId(int orderId) {
        String sql = "SELECT * FROM invoices WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoice by order ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updatePaymentStatus(int invoiceId, boolean isPaid, String paymentMethod) {
        String sql = "UPDATE invoices SET is_paid = ?, payment_method = ?, payment_date = ? WHERE invoice_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isPaid);
            pstmt.setString(2, paymentMethod);
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(4, invoiceId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating invoice payment status: " + e.getMessage());
        }
        return false;
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        int invoiceId = rs.getInt("invoice_id");
        int orderId = rs.getInt("order_id");
        double amount = rs.getDouble("amount");
        String paymentMethod = rs.getString("payment_method");
        Timestamp paymentDate = rs.getTimestamp("payment_date");
        boolean isPaid = rs.getBoolean("is_paid");

        Invoice invoice = new Invoice(invoiceId, amount, paymentMethod, paymentDate);
        invoice.setPaid(isPaid);

        if (orderId > 0) {
            OrderDAO orderDao = new OrderDAO();
            Order order = orderDao.getOrderById(orderId);
            invoice.setOrder(order);
        }

        return invoice;
    }
}