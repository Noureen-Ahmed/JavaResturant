package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class UITheme {
    public static final Color COLOR_BG = new Color(0xF5, 0xEB, 0xDD);        
    public static final Color COLOR_DARK_BROWN = new Color(0x5C, 0x40, 0x33); 
    public static final Color COLOR_BROWN = new Color(0x8B, 0x5E, 0x3C);      
    public static final Color COLOR_CREAM = new Color(0xFF, 0xF8, 0xEF);      
    public static final Color COLOR_TEXT = new Color(0x3E, 0x27, 0x23);       
    public static final Color COLOR_RED = new Color(0xA9, 0x32, 0x26);       
    static {
        UIManager.put("Label.foreground", COLOR_TEXT);
        UIManager.put("Button.foreground", COLOR_TEXT);
        UIManager.put("TableHeader.foreground", COLOR_TEXT);
        UIManager.put("Table.foreground", COLOR_TEXT);
        UIManager.put("TextField.foreground", COLOR_TEXT);
        UIManager.put("PasswordField.foreground", COLOR_TEXT);
        UIManager.put("TextArea.foreground", COLOR_TEXT);
        UIManager.put("ComboBox.foreground", COLOR_TEXT);
        UIManager.put("TabbedPane.foreground", COLOR_TEXT);
        UIManager.put("TitledBorder.titleColor", COLOR_TEXT);
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_CREAM);
        btn.setForeground(COLOR_TEXT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BROWN, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CREAM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BROWN, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(COLOR_TEXT);
        
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(COLOR_CREAM);
        table.getTableHeader().setForeground(COLOR_TEXT); 
        table.getTableHeader().setReorderingAllowed(false);
        
        table.setSelectionBackground(COLOR_BG);
        table.setSelectionForeground(COLOR_TEXT); 
        table.setGridColor(COLOR_BROWN);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        renderer.setForeground(COLOR_TEXT); 

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        return table;
    }
}
