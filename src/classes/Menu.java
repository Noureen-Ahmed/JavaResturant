import java.util.ArrayList;
import java.util.List;

public class Menu {
    private List<FoodItem> items;

    public Menu() {
        items = new ArrayList<>();
    }

    public void addItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }

        // Prevent duplicate food items with the same ID
        for (FoodItem existingItem : items) {
            if (existingItem.getId() == item.getId()) {
                throw new IllegalArgumentException(
                        "A food item with this ID already exists"
                );
            }
        }

        items.add(item);
    }

    public void removeItem(int itemId) {
        if (itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }

        boolean removed = items.removeIf(item -> item.getId() == itemId);

        if (!removed) {
            throw new IllegalArgumentException(
                    "No food item found with ID: " + itemId
            );
        }
    }

    public void displayMenu() {
        if (items.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        for (FoodItem item : items) {
            System.out.println(item);
        }
    }

    public List<FoodItem> getItems() {
        return new ArrayList<>(items);
    }
}