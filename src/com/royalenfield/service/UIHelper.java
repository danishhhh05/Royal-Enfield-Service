package com.royalenfield.service;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Reusable styled Swing components.
 */
public final class UIHelper {

    public static final int CORNER = 16;
    public static final int BTN_RADIUS = 12;
    public static final int FIELD_RADIUS = 10;

    private UIHelper() {
    }

    public static Font plain(int size) {
        return Dashboard.plainFont(size);
    }

    public static Font bold(int size) {
        return Dashboard.boldFont(size);
    }

    public static JLabel title(String text) {
        JLabel lbl = Dashboard.label(text, bold(AppColors.sizeSubheading()), AppColors.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 12, 0));
        return lbl;
    }

    public static JLabel fieldLabel(String text) {
        return Dashboard.label(text, plain(AppColors.sizeSmall()), AppColors.TEXT_SECONDARY);
    }

    public static JTextField textField(String placeholder) {
        return new RoundedTextField(placeholder);
    }

    public static JComboBox<String> combo(String... items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(plain(AppColors.sizeBody()));
        box.setBackground(AppColors.SURFACE_LIGHT);
        box.setForeground(AppColors.TEXT_PRIMARY);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        return box;
    }

    public static JButton primaryButton(String text) {
        return Dashboard.styledButton(text, AppColors.ACCENT_RED, AppColors.ACCENT_RED_BRIGHT);
    }

    public static JButton secondaryButton(String text) {
        return Dashboard.styledButton(text, AppColors.SURFACE_LIGHT, AppColors.SURFACE_ELEVATED);
    }

    public static JButton dangerButton(String text) {
        return Dashboard.styledButton(text, AppColors.ACCENT_RED_DARK, AppColors.ACCENT_RED);
    }

    public static JPanel card() {
        Dashboard.RoundedPanel p = new Dashboard.RoundedPanel(AppColors.SURFACE, CORNER);
        p.setBorder(new EmptyBorder(20, 22, 20, 22));
        return p;
    }

    public static JScrollPane wrapTable(JTable table) {
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppColors.TABLE_ROW);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        return scroll;
    }

    public static void styleTable(JTable table) {
        table.setBackground(AppColors.TABLE_ROW);
        table.setForeground(AppColors.TEXT_PRIMARY);
        table.setFont(plain(AppColors.sizeBody()));
        table.setSelectionBackground(AppColors.ACCENT_RED);
        table.setSelectionForeground(AppColors.TEXT_ON_ACCENT);
        table.setGridColor(AppColors.TABLE_GRID);

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColors.TABLE_HEADER);
        header.setForeground(AppColors.TEXT_PRIMARY);
        header.setFont(bold(AppColors.sizeBody()));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean selected,
                    boolean focused, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, selected, focused, row, col);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? AppColors.TABLE_ROW : AppColors.TABLE_ROW_ALT);
                }
                c.setForeground(AppColors.TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        table.addMouseMotionListener(new MouseAdapter() {
            int lastRow = -1;

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != lastRow) {
                    lastRow = row;
                    table.repaint();
                }
            }
        });
    }

    public static DefaultTableModel tableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JPanel formRow(String label, Component field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.add(fieldLabel(label), BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        return row;
    }

    public static void showError(Component parent, String message) {
        javax.swing.JOptionPane.showMessageDialog(parent, message, "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        javax.swing.JOptionPane.showMessageDialog(parent, message, "Success",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    static class RoundedTextField extends JTextField {
        private final String placeholder;
        private boolean focused;

        RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setFont(plain(AppColors.sizeBody()));
            setForeground(AppColors.TEXT_PRIMARY);
            setCaretColor(AppColors.ACCENT_RED);
            setBorder(new EmptyBorder(10, 14, 10, 14));

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColors.SURFACE_LIGHT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), FIELD_RADIUS, FIELD_RADIUS);
            g2.setColor(focused ? AppColors.BORDER_FOCUS : AppColors.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, FIELD_RADIUS, FIELD_RADIUS);
            g2.dispose();

            if (getText().isEmpty() && placeholder != null && !focused) {
                Graphics2D g2p = (Graphics2D) g.create();
                g2p.setColor(AppColors.TEXT_MUTED);
                g2p.setFont(getFont());
                g2p.drawString(placeholder, 14, g.getFontMetrics().getAscent() + 10);
                g2p.dispose();
            }
            super.paintComponent(g);
        }
    }
}
