package classes;

import java.util.Date;


public class Reservation {
    private int id;
    private String customerName;
    private String phone;
    private Date date;
    private int tableNumber;

    public Reservation() {
        this.date = new Date();
        this.phone = "";
    }

    public Reservation(String customerName, String phone, Date date, int tableNumber) {
        setCustomerName(customerName);
        setPhone(phone);
        setDate(date);
        setTableNumber(tableNumber);
    }

    public Reservation(int id, String customerName, String phone, Date date, int tableNumber) {
        this(customerName, phone, date, tableNumber);
        setId(id);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Reservation ID cannot be negative.");
        }
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty.");
        }
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date != null ? date : new Date();
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        if (tableNumber <= 0) {
            throw new IllegalArgumentException("Table number must be positive.");
        }
        this.tableNumber = tableNumber;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", phone='" + phone + '\'' +
                ", date=" + date +
                ", tableNumber=" + tableNumber +
                '}';
    }
}