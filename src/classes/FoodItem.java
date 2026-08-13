package classes;
/**
 * Represents a menu food item in the restaurant system.
 */
public class FoodItem {
    private int id;
    private String name;
    private double price;
    private String category;

    public FoodItem() {}

    public FoodItem(String name, double price, String category) {
        setName(name);
        setPrice(price);
        setCategory(category);
    }

    public FoodItem(int id, String name, double price, String category) {
        setId(id);
        setName(name);
        setPrice(price);
        setCategory(category);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) { 
            throw new IllegalArgumentException("FoodItem ID cannot be negative.");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("FoodItem name cannot be null or empty.");
        }
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("FoodItem price cannot be negative.");
        }
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("FoodItem category cannot be null or empty.");
        }
        this.category = category;
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                '}';
    }
}