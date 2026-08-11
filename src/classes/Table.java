package classes;

/**
 * Represents a dining table in the restaurant.
 */
public class Table {
    private int tableNumber;
    private boolean available;

    // 1. Default Constructor
    public Table() {
        this.available = true;
    }

    // 2. Constructor برقم الطاولة فقط
    public Table(int tableNumber) {
        this(tableNumber, true);
    }

    // 3. Constructor كامل
    public Table(int tableNumber, boolean available) {
        setTableNumber(tableNumber);
        setAvailable(available);
    }

    // --- Getters & Setters ---

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        if (tableNumber <= 0) {
            throw new IllegalArgumentException("Table number must be positive.");
        }
        this.tableNumber = tableNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableNumber=" + tableNumber +
                ", available=" + available +
                '}';
    }
}