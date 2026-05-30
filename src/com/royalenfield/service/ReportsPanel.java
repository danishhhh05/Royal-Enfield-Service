package com.royalenfield.service;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Summary reports and recent service listing.
 */
public class ReportsPanel extends JPanel {

    private final JLabel customersLbl = statLabel("0");
    private final JLabel servicesLbl = statLabel("0");
    private final JLabel activeLbl = statLabel("0");
    private final JLabel revenueLbl = statLabel("₹ 0");
    private final DefaultTableModel recentModel = UIHelper.tableModel(
            new String[]{"ID", "Customer", "Bike", "Status", "Amount", "Date"});
    private final JTable recentTable = new JTable(recentModel);

    public ReportsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        Dashboard.RoundedPanel header = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        header.add(UIHelper.title("Reports & Analytics"), BorderLayout.WEST);
        javax.swing.JButton refresh = UIHelper.secondaryButton("Refresh");
        refresh.addActionListener(e -> loadReport());
        header.add(refresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildSummaryCards(), BorderLayout.NORTH);

        Dashboard.RoundedPanel tableCard = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel sub = Dashboard.label("Recent Service Records", UIHelper.bold(AppColors.sizeBody()),
                AppColors.TEXT_SECONDARY);
        sub.setBorder(new EmptyBorder(0, 8, 8, 0));
        tableCard.add(sub, BorderLayout.NORTH);
        tableCard.add(UIHelper.wrapTable(recentTable), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        loadReport();
    }

    private JPanel buildSummaryCards() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);
        grid.add(summaryCard("Total Customers", customersLbl, AppColors.CARD_CUSTOMERS));
        grid.add(summaryCard("Total Services", servicesLbl, AppColors.CARD_BIKES));
        grid.add(summaryCard("Active Services", activeLbl, AppColors.CARD_SERVICES));
        grid.add(summaryCard("Total Revenue", revenueLbl, AppColors.CARD_REVENUE));
        return grid;
    }

    private JPanel summaryCard(String title, JLabel value, java.awt.Color accent) {
        Dashboard.RoundedPanel card = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        JLabel t = Dashboard.label(title, UIHelper.plain(AppColors.sizeSmall()), AppColors.TEXT_SECONDARY);
        value.setFont(UIHelper.bold(AppColors.sizeStatValue()));
        value.setForeground(AppColors.TEXT_PRIMARY);
        card.add(t, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        JPanel bar = new JPanel();
        bar.setOpaque(false);
        bar.setPreferredSize(new java.awt.Dimension(0, 4));
        bar.setBackground(accent);
        card.add(bar, BorderLayout.SOUTH);
        return card;
    }

    private static JLabel statLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        return lbl;
    }

    public void loadReport() {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        customersLbl.setText(String.valueOf(DBConnection.countCustomers()));
        servicesLbl.setText(String.valueOf(DBConnection.countBikesServiced()));
        activeLbl.setText(String.valueOf(DBConnection.countActiveServices()));
        revenueLbl.setText(currency.format(DBConnection.totalRevenue()));

        recentModel.setRowCount(0);
        if (!DBConnection.isConnected()) {
            return;
        }
        try {
            List<ServiceRecord> list = ServiceDAO.findAll();
            int limit = Math.min(15, list.size());
            for (int i = 0; i < limit; i++) {
                ServiceRecord r = list.get(i);
                recentModel.addRow(new Object[]{
                        r.getId(),
                        r.getCustomerName(),
                        r.getBikeModel(),
                        r.getStatus(),
                        String.format("₹ %.2f", r.getAmount()),
                        r.getServiceDate()
                });
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }
}
