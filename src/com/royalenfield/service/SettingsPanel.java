package com.royalenfield.service;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;

/**
 * Database connection settings — edit and test {@code db.properties}.
 */
public class SettingsPanel extends JPanel {

    private final Runnable onConnectionChanged;
    private final JTextField hostField = UIHelper.textField("localhost");
    private final JTextField portField = UIHelper.textField("3306");
    private final JTextField dbField = UIHelper.textField("re_service_db");
    private final JTextField userField = UIHelper.textField("root");
    private final JTextField passField = UIHelper.textField("password");
    private final JLabel statusLabel = Dashboard.label("", UIHelper.plain(AppColors.sizeBody()), AppColors.TEXT_SECONDARY);

    public SettingsPanel(Runnable onConnectionChanged) {
        this.onConnectionChanged = onConnectionChanged;
        setOpaque(false);
        setLayout(new BorderLayout());

        Dashboard.RoundedPanel card = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setMaximumSize(new java.awt.Dimension(520, Integer.MAX_VALUE));

        card.add(UIHelper.title("Database Settings"));
        card.add(Dashboard.label(
                "Configure MySQL connection. Settings are saved to db.properties in the project folder.",
                UIHelper.plain(AppColors.sizeBody()), AppColors.TEXT_SECONDARY));
        card.add(Box.createVerticalStrut(20));

        card.add(UIHelper.formRow("Host", hostField));
        card.add(Box.createVerticalStrut(10));
        card.add(UIHelper.formRow("Port", portField));
        card.add(Box.createVerticalStrut(10));
        card.add(UIHelper.formRow("Database Name", dbField));
        card.add(Box.createVerticalStrut(10));
        card.add(UIHelper.formRow("Username", userField));
        card.add(Box.createVerticalStrut(10));
        card.add(UIHelper.formRow("Password", passField));
        card.add(Box.createVerticalStrut(16));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        JButton test = UIHelper.secondaryButton("Test Connection");
        JButton save = UIHelper.primaryButton("Save & Connect");
        JButton init = UIHelper.secondaryButton("Create Tables");
        test.addActionListener(e -> testConnection());
        save.addActionListener(e -> saveSettings());
        init.addActionListener(e -> initSchema());
        buttons.add(test);
        buttons.add(save);
        buttons.add(init);
        card.add(buttons);
        card.add(Box.createVerticalStrut(12));
        card.add(statusLabel);

        card.add(Box.createVerticalStrut(24));
        card.add(buildHelpPanel());

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
        center.setOpaque(false);
        center.add(card);
        add(center, BorderLayout.NORTH);

        loadFromConfig();
    }

    private JPanel buildHelpPanel() {
        Dashboard.RoundedPanel help = new Dashboard.RoundedPanel(AppColors.SURFACE_ELEVATED, UIHelper.CORNER);
        help.setLayout(new BoxLayout(help, BoxLayout.Y_AXIS));
        help.setBorder(new EmptyBorder(16, 18, 16, 18));
        String[] lines = {
                "1. Install MySQL Server and start the service",
                "2. Default user: root (set your MySQL password above)",
                "3. Place mysql-connector-j.jar in the lib folder",
                "4. Click Save & Connect, then Create Tables",
                "5. Config file: " + DBConnection.getConfigFile().getAbsolutePath()
        };
        for (String line : lines) {
            help.add(Dashboard.label(line, UIHelper.plain(AppColors.sizeSmall()), AppColors.TEXT_MUTED));
            help.add(Box.createVerticalStrut(4));
        }
        return help;
    }

    private void loadFromConfig() {
        DBConnection.loadConfig();
        hostField.setText(DBConnection.getHost());
        portField.setText(DBConnection.getPort());
        dbField.setText(DBConnection.getDatabase());
        userField.setText(DBConnection.getUser());
        passField.setText(DBConnection.getPassword());
        updateStatus(DBConnection.isConnected());
    }

    private void testConnection() {
        DBConnection.applyConfig(
                hostField.getText().trim(),
                portField.getText().trim(),
                dbField.getText().trim(),
                userField.getText().trim(),
                passField.getText());
        if (DBConnection.testConnection()) {
            updateStatus(true);
            UIHelper.showInfo(this, "Connection successful!");
        } else {
            updateStatus(false);
            UIHelper.showError(this, "Connection failed:\n" + DBConnection.getLastError());
        }
    }

    private void saveSettings() {
        try {
            DBConnection.saveConfig(
                    hostField.getText().trim(),
                    portField.getText().trim(),
                    dbField.getText().trim(),
                    userField.getText().trim(),
                    passField.getText());
            if (DBConnection.testConnection()) {
                updateStatus(true);
                UIHelper.showInfo(this, "Settings saved and connected.");
                notifyChange();
            } else {
                updateStatus(false);
                UIHelper.showError(this, "Saved but connection failed:\n" + DBConnection.getLastError());
            }
        } catch (IOException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void initSchema() {
        DBConnection.applyConfig(
                hostField.getText().trim(),
                portField.getText().trim(),
                dbField.getText().trim(),
                userField.getText().trim(),
                passField.getText());
        try {
            DBConnection.initializeSchema();
            updateStatus(true);
            UIHelper.showInfo(this, "Database and tables are ready.");
            notifyChange();
        } catch (Exception ex) {
            updateStatus(false);
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void updateStatus(boolean ok) {
        statusLabel.setText(ok ? "\u25CF Connected to MySQL" : "\u25CF Not connected — check settings");
        statusLabel.setForeground(ok ? AppColors.SUCCESS : AppColors.ACCENT_ORANGE);
    }

    private void notifyChange() {
        if (onConnectionChanged != null) {
            onConnectionChanged.run();
        }
    }
}
