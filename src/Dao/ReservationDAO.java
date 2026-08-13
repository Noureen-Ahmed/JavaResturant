package Dao;

import classes.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public boolean addReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (customer_name, phone, reservation_date, table_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, reservation.getCustomerName());
            pstmt.setString(2, reservation.getPhone());
            
            Timestamp resTimestamp = reservation.getDate() != null 
                    ? new Timestamp(reservation.getDate().getTime()) 
                    : new Timestamp(System.currentTimeMillis());
            pstmt.setTimestamp(3, resTimestamp);
            
            pstmt.setInt(4, reservation.getTableNumber());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        reservation.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding reservation: " + e.getMessage());
        }
        return false;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservations: " + e.getMessage());
        }
        return list;
    }

    public Reservation getReservationById(int id) {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservation by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteReservation(int id) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM reservations WHERE id = ?")) {
                pstmt.setInt(1, id);
                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            renumberReservations(conn);
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting reservation: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    private void renumberReservations(Connection conn) throws SQLException {
        List<Integer> oldIds = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM reservations ORDER BY id")) {
            while (rs.next()) oldIds.add(rs.getInt(1));
        }
        if (oldIds.isEmpty()) return;

        int newId = 1;
        for (int oldId : oldIds) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE reservations SET id = ? WHERE id = ?")) {
                ps.setInt(1, newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            newId++;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE reservations AUTO_INCREMENT = 1");
        }
    }

    public boolean isPhoneExists(String phone, int excludeId) {
        String sql = "SELECT 1 FROM reservations WHERE phone = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking reservation phone existence: " + e.getMessage());
        }
        return false;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String customerName = rs.getString("customer_name");
        String phone = rs.getString("phone");
        Timestamp date = rs.getTimestamp("reservation_date");
        int tableNumber = rs.getInt("table_number");

        return new Reservation(id, customerName, phone, date, tableNumber);
    }
}