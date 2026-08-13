package Dao;

import classes.Employee;
import classes.Manager;
import classes.Cashier;
import classes.DeliveryMan;
import classes.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (name, phone, username, password, role, salary, is_available) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getPhone());
            pstmt.setString(3, employee.getUsername());
            pstmt.setString(4, employee.getPassword());
            pstmt.setString(5, employee.getRole().name());
            pstmt.setDouble(6, employee.getSalary());

            if (employee instanceof DeliveryMan) {
                pstmt.setBoolean(7, ((DeliveryMan) employee).isAvailable());
            } else {
                pstmt.setBoolean(7, true);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        employee.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employees: " + e.getMessage());
        }
        return list;
    }

    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET name = ?, phone = ?, username = ?, password = ?, role = ?, salary = ?, is_available = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getPhone());
            pstmt.setString(3, employee.getUsername());
            pstmt.setString(4, employee.getPassword());
            pstmt.setString(5, employee.getRole().name());
            pstmt.setDouble(6, employee.getSalary());

            if (employee instanceof DeliveryMan) {
                pstmt.setBoolean(7, ((DeliveryMan) employee).isAvailable());
            } else {
                pstmt.setBoolean(7, true);
            }
            
            pstmt.setInt(8, employee.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteEmployee(int id) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM employees WHERE id = ?")) {
                pstmt.setInt(1, id);
                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            renumberEmployees(conn);
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
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

    private void renumberEmployees(Connection conn) throws SQLException {
        List<Integer> oldIds = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM employees ORDER BY id")) {
            while (rs.next()) oldIds.add(rs.getInt(1));
        }
        if (oldIds.isEmpty()) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        int newId = 1;
        for (int oldId : oldIds) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET cashier_id = ? WHERE cashier_id = ?")) {
                ps.setInt(1, -newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE employees SET id = ? WHERE id = ?")) {
                ps.setInt(1, newId);
                ps.setInt(2, oldId);
                ps.executeUpdate();
            }
            newId++;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE orders SET cashier_id = ABS(cashier_id)");
            stmt.execute("ALTER TABLE employees AUTO_INCREMENT = 1");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    public boolean isPhoneExists(String phone, int excludeId) {
        String sql = "SELECT 1 FROM employees WHERE phone = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking phone existence: " + e.getMessage());
        }
        return false;
    }

    public boolean isUsernameExists(String username, int excludeId) {
        String sql = "SELECT 1 FROM employees WHERE username = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
        }
        return false;
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String username = rs.getString("username");
        String password = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        boolean available = rs.getBoolean("is_available");
        double salary = rs.getDouble("salary");

        Employee emp;
        switch (role) {
            case MANAGER:
                emp = new Manager(id, name, phone, username, password);
                break;
            case CASHIER:
                emp = new Cashier(id, name, phone, username, password);
                break;
            case DELIVERY:
                DeliveryMan deliveryMan = new DeliveryMan(id, name, phone, username, password);
                deliveryMan.setAvailable(available); 
                emp = deliveryMan;
                break;
            default:
                throw new IllegalArgumentException("Unknown Role: " + role);
        }
        emp.setSalary(salary);
        return emp;
    }

}