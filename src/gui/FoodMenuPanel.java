package gui;

import Dao.FoodItemDAO;
import classes.FoodItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Food Menu GUI Panel displaying database food items in a JTable.
 * Features Search field, Search button, and Refresh button.
 */
public class FoodMenuPanel extends JPanel {

    private final FoodItemDAO foodItemDao = new FoodItemDAO();
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public FoodMenuPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("🍔 Food Menu");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);
        add(titleLabel, BorderLayout.NORTH);

        JPanel topToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topToolBar.setBackground(UITheme.COLOR_BG);

        searchField = new JTextField(20);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton searchBtn = UITheme.createButton("Search", UITheme.COLOR_DARK_BROWN, UITheme.COLOR_CREAM);
        JButton refreshBtn = UITheme.createButton("Refresh", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);

        topToolBar.add(new JLabel("Search Food:"));
        topToolBar.add(searchField);
        topToolBar.add(searchBtn);
        topToolBar.add(refreshBtn);

        String[] columns = {"ID", "Food Name", "Category", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable foodTable = UITheme.createStyledTable(tableModel);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(topToolBar, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(foodTable), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadFoodData());
        searchBtn.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());

        loadFoodData();
    }

    public void loadFoodData() {
        tableModel.setRowCount(0);
        searchField.setText("");
        List<FoodItem> items = foodItemDao.getAllFoodItems();
        for (FoodItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    String.format("$%.2f", item.getPrice())
            });
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadFoodData();
            return;
        }

        tableModel.setRowCount(0);
        List<FoodItem> items = foodItemDao.getAllFoodItems();
        for (FoodItem item : items) {
            if (item.getName().toLowerCase().contains(query) ||
                item.getCategory().toLowerCase().contains(query)) {
                tableModel.addRow(new Object[]{
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        String.format("$%.2f", item.getPrice())
                });
            }
        }
    }
}
