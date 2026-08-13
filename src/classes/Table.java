package classes;


public class Table {
    private int tableNumber;
    private int capacity;
    private boolean available;

    public Table() {
        this.available = true;
    }

    public Table(int tableNumber) {
        this(tableNumber, 4, true);
    }

    public Table(int tableNumber, int capacity, boolean available) {
        setTableNumber(tableNumber);
        setCapacity(capacity);
        setAvailable(available);
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.capacity = capacity;
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
                ", capacity=" + capacity +
                ", available=" + available +
                '}';
    }
}