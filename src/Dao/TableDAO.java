package Dao;

import classes.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    // 1. Add Table
    public boolean addTable(Table table) {
        String sql = "INSERT INTO restaurant_tables (table_number, capacity, is_available) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity()); // ⬅️ قراءة الـ capacity الديناميكية من الـ Object
            pstmt.setBoolean(3, table.isAvailable());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding table: " + e.getMessage());
        }
        return false;
    }

    // 2. Get All Tables
    public List<Table> getAllTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_tables";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToTable(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tables: " + e.getMessage());
        }
        return list;
    }

    // 3. Get Table By Number
    public Table getTableByNumber(int tableNumber) {
        String sql = "SELECT * FROM restaurant_tables WHERE table_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tableNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTable(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching table: " + e.getMessage());
        }
        return null;
    }

    // 4. Update Table Status
    public boolean updateTableStatus(Table table) {
        String sql = "UPDATE restaurant_tables SET is_available = ? WHERE table_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, table.isAvailable());
            pstmt.setInt(2, table.getTableNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating table: " + e.getMessage());
        }
        return false;
    }

    // 5. Delete Table
    public boolean deleteTable(int tableNumber) {
        String sql = "DELETE FROM restaurant_tables WHERE table_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tableNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting table: " + e.getMessage());
        }
        return false;
    }

    // Helper Method
    private Table mapResultSetToTable(ResultSet rs) throws SQLException {
        int tableNumber = rs.getInt("table_number");
        int capacity = rs.getInt("capacity"); // ⬅️ قراءة سعة الطاولة من الداتا بيز
        boolean available = rs.getBoolean("is_available");

        return new Table(tableNumber, capacity, available); // ⬅️ التمرير للكونستراكتور الجديد
    }
}