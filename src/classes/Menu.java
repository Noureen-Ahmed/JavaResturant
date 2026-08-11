package classes;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private List<FoodItem> items;

    public Menu() {
        this.items = new ArrayList<>();
    }

    public Menu(List<FoodItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(FoodItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }

        // Prevent duplicate food items with the same positive ID
        for (FoodItem existingItem : items) {
            if (existingItem != null && existingItem.getId() == item.getId() && item.getId() > 0) {
                throw new IllegalArgumentException("A food item with this ID already exists");
            }
        }

        items.add(item);
    }

    public void removeItem(int itemId) {
        if (itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }

        boolean removed = items.removeIf(item -> item != null && item.getId() == itemId);

        if (!removed) {
            throw new IllegalArgumentException("No food item found with ID: " + itemId);
        }
    }

    public void displayMenu() {
        if (items.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        for (FoodItem item : items) {
            if (item != null) {
                System.out.println(item);
            }
        }
    }

    public List<FoodItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<FoodItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}