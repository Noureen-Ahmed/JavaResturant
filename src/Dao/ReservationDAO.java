package Dao;

import classes.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    // 1. Add Reservation
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

    // 2. Get All Reservations
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

    // 3. Get Reservation By ID
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

    // 4. Delete Reservation
    public boolean deleteReservation(int id) {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting reservation: " + e.getMessage());
        }
        return false;
    }

    // Helper Method
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String customerName = rs.getString("customer_name");
        String phone = rs.getString("phone");
        Timestamp date = rs.getTimestamp("reservation_date");
        int tableNumber = rs.getInt("table_number");

        return new Reservation(id, customerName, phone, date, tableNumber);
    }
}