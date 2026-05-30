package com.royalenfield.service;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Application entry point — Royal Enfield Service Management System.
 */
public final class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applyGlobalTheme();
            String dbMessage = null;
            try {
                DBConnection.initializeSchema();
            } catch (Exception ex) {
                dbMessage = ex.getMessage();
            }
            Dashboard dashboard = new Dashboard();
            dashboard.setVisible(true);
            if (dbMessage != null) {
                JOptionPane.showMessageDialog(
                        dashboard,
                        "Could not connect to MySQL on startup.\n"
                                + "Open Settings to configure the database.\n\n"
                                + dbMessage,
                        "Database",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * Baseline Swing defaults before custom-painted components take over.
     */
    private static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Font base = new Font(AppColors.FONT_FAMILY, Font.PLAIN, AppColors.sizeBody());
        if (!base.getFamily().equals(AppColors.FONT_FAMILY)) {
            base = new Font(AppColors.FONT_FALLBACK, Font.PLAIN, AppColors.sizeBody());
        }

        UIManager.put("OptionPane.background", AppColors.SURFACE);
        UIManager.put("Panel.background", AppColors.BACKGROUND);
        UIManager.put("Label.foreground", AppColors.TEXT_PRIMARY);
        UIManager.put("Label.font", base);
        UIManager.put("Button.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("ComboBox.font", base);
        UIManager.put("Table.font", base);
        UIManager.put("TableHeader.font", base.deriveFont(Font.BOLD));
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBar.thumb", AppColors.SURFACE_LIGHT);
        UIManager.put("ScrollBar.track", AppColors.SURFACE);
        UIManager.put("ToolTip.background", AppColors.SURFACE_ELEVATED);
        UIManager.put("ToolTip.foreground", AppColors.TEXT_PRIMARY);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(AppColors.BORDER));

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }
}
