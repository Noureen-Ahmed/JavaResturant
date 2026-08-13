package classes;

public abstract class Employee {
    private int id;
    private String name;
    private String phone;
    private String username;
    private String password;
    private Role role;
    private boolean loggedIn;
    private double salary;

    public Employee() {

        this.loggedIn = false;
    }

    public Employee(String name, String phone, String username, String password, Role role) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Employee role cannot be null.");
        }

        this.name = name;
        this.phone = phone != null ? phone : "";
        this.username = username;
        this.password = password;
        this.role = role;
        this.loggedIn = false;
    }

    public Employee(int id, String name, String phone, String username, String password, Role role) {
        this(name, phone, username, password, role);
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be a positive integer.");
        }
        this.id = id;
    }

    public boolean login(String username, String password) {
        if (username != null && password != null
                && this.username != null && this.password != null
                && this.username.equals(username)
                && this.password.equals(password)) {
            this.loggedIn = true;
            return true;
        }
        return false;
    }

    public void logout() {
        this.loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be a positive integer.");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty.");
        }
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        this.password = password;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative.");
        }
        this.salary = salary;
    }
}