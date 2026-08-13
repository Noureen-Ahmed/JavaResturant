package gui;

import classes.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;


public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JLabel welcomeUserLabel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel welcomeCard = UITheme.createCardPanel();
        welcomeCard.setLayout(new BoxLayout(welcomeCard, BoxLayout.Y_AXIS));
        welcomeCard.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("Welcome to Our Restaurant!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(UITheme.COLOR_DARK_BROWN);

        welcomeUserLabel = new JLabel("Logged in user: Guest");
        welcomeUserLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        welcomeUserLabel.setForeground(UITheme.COLOR_TEXT);

        welcomeCard.add(titleLabel);
        welcomeCard.add(Box.createRigidArea(new Dimension(0, 10)));
        welcomeCard.add(welcomeUserLabel);

        add(welcomeCard, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        gridPanel.setBackground(UITheme.COLOR_BG);
        gridPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        gridPanel.add(createDashboardCard("🍔 Food Menu", "View available food items and menu pricing.", e -> mainFrame.showPanel("FOOD_MENU")));
        gridPanel.add(createDashboardCard("🛒 New Order", "Place new orders and generate customer invoices.", e -> mainFrame.showPanel("NEW_ORDER")));
        gridPanel.add(createDashboardCard("🪑 Tables & Reservations", "Manage table availability and bookings.", e -> mainFrame.showPanel("TABLES")));

        add(gridPanel, BorderLayout.CENTER);
    }

    public void updateUserInfo(Employee user) {
        if (user != null) {
            welcomeUserLabel.setText("Logged in as: " + user.getName() + " (" + user.getRole() + ")");
        } else {
            welcomeUserLabel.setText("Logged in user: Guest");
        }
    }

    private JPanel createDashboardCard(String title, String desc, ActionListener action) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(UITheme.COLOR_DARK_BROWN);

        JLabel descLbl = new JLabel("<html><body style='width: 180px;'>" + desc + "</body></html>");
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLbl.setForeground(UITheme.COLOR_TEXT);

        textPanel.add(titleLbl);
        textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        textPanel.add(descLbl);

        JButton openBtn = UITheme.createButton("Open", UITheme.COLOR_BROWN, UITheme.COLOR_CREAM);
        openBtn.addActionListener(action);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(openBtn, BorderLayout.SOUTH);

        return card;
    }
}
