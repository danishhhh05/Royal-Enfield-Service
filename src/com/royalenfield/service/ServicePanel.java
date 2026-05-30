package com.royalenfield.service;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Service records — table + CRUD form linked to customers.
 */
public class ServicePanel extends JPanel {

    private static final String[] STATUSES = {"Pending", "In Progress", "Completed", "Cancelled"};

    private final Runnable onDataChanged;
    private final DefaultTableModel tableModel = UIHelper.tableModel(
            new String[]{"ID", "Customer", "Bike", "Type", "Status", "Amount", "Date"});
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = UIHelper.textField("Search services...");
    private final JComboBox<Customer> customerCombo = new JComboBox<>();
    private final JTextField bikeField = UIHelper.textField("e.g. Classic 350");
    private final JComboBox<String> typeCombo = UIHelper.combo(
            "General Service", "Engine Tune-up", "Brake Service", "Oil Change", "Tyre Change", "Custom");
    private final JComboBox<String> statusCombo = UIHelper.combo(STATUSES);
    private final JTextField amountField = UIHelper.textField("0.00");
    private final JTextField dateField = UIHelper.textField("YYYY-MM-DD");
    private final JTextField notesField = UIHelper.textField("Notes");

    private int selectedId = -1;

    public ServicePanel(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        reload();
    }

    private JPanel buildToolbar() {
        Dashboard.RoundedPanel bar = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        bar.setLayout(new BorderLayout(12, 0));
        bar.setBorder(new EmptyBorder(14, 18, 14, 18));
        bar.add(UIHelper.title("Service Records"), BorderLayout.WEST);

        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchField.setPreferredSize(new Dimension(260, 38));
        JButton searchBtn = UIHelper.secondaryButton("Search");
        searchBtn.addActionListener(e -> loadTable(searchField.getText()));
        searchField.addActionListener(e -> loadTable(searchField.getText()));
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBox.add(searchBtn, BorderLayout.EAST);
        bar.add(searchBox, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);

        Dashboard.RoundedPanel tableCard = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(8, 8, 8, 8));
        tableCard.add(UIHelper.wrapTable(table), BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        Dashboard.RoundedPanel formCard = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        formCard.setPreferredSize(new Dimension(320, 0));
        formCard.add(buildForm(), BorderLayout.CENTER);
        content.add(formCard, BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fillForm(table.getSelectedRow());
            }
        });
        return content;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(UIHelper.title("Service Details"));

        customerCombo.setFont(UIHelper.plain(AppColors.sizeBody()));
        customerCombo.setBackground(AppColors.SURFACE_LIGHT);
        customerCombo.setForeground(AppColors.TEXT_PRIMARY);
        form.add(UIHelper.formRow("Customer *", customerCombo));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Bike Model *", bikeField));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Service Type", typeCombo));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Status", statusCombo));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Amount (₹)", amountField));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Service Date", dateField));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Notes", notesField));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        JButton add = UIHelper.primaryButton("Add");
        JButton update = UIHelper.secondaryButton("Update");
        JButton delete = UIHelper.dangerButton("Delete");
        JButton clear = UIHelper.secondaryButton("Clear");
        add.addActionListener(e -> addRecord());
        update.addActionListener(e -> updateRecord());
        delete.addActionListener(e -> deleteRecord());
        clear.addActionListener(e -> clearForm());
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(clear);
        form.add(Box.createVerticalStrut(12));
        form.add(buttons);
        return form;
    }

    public void reload() {
        loadCustomers();
        loadTable(searchField.getText());
    }

    public void focusNewService() {
        clearForm();
        dateField.setText(LocalDate.now().toString());
        customerCombo.requestFocusInWindow();
    }

    public void clearForm() {
        selectedId = -1;
        bikeField.setText("");
        amountField.setText("0");
        dateField.setText(LocalDate.now().toString());
        notesField.setText("");
        statusCombo.setSelectedItem("Pending");
        if (customerCombo.getItemCount() > 0) {
            customerCombo.setSelectedIndex(0);
        }
        table.clearSelection();
    }

    private void loadCustomers() {
        customerCombo.removeAllItems();
        if (!DBConnection.isConnected()) {
            return;
        }
        try {
            for (Customer c : CustomerDAO.findAll()) {
                customerCombo.addItem(c);
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void loadTable(String query) {
        if (!DBConnection.isConnected()) {
            tableModel.setRowCount(0);
            return;
        }
        try {
            List<ServiceRecord> records = ServiceDAO.findAll(query);
            tableModel.setRowCount(0);
            for (ServiceRecord r : records) {
                tableModel.addRow(new Object[]{
                        r.getId(),
                        r.getCustomerName(),
                        r.getBikeModel(),
                        r.getServiceType(),
                        r.getStatus(),
                        String.format("₹ %.2f", r.getAmount()),
                        r.getServiceDate()
                });
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Failed to load services: " + ex.getMessage());
        }
    }

    private void fillForm(int row) {
        selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String customerName = tableModel.getValueAt(row, 1).toString();
        for (int i = 0; i < customerCombo.getItemCount(); i++) {
            Customer c = customerCombo.getItemAt(i);
            if (c.getName().equals(customerName)) {
                customerCombo.setSelectedIndex(i);
                break;
            }
        }
        bikeField.setText(str(row, 2));
        typeCombo.setSelectedItem(str(row, 3));
        statusCombo.setSelectedItem(str(row, 4));
        String amt = str(row, 5).replace("₹", "").trim();
        amountField.setText(amt);
        dateField.setText(str(row, 6));
    }

    private String str(int row, int col) {
        Object v = tableModel.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private ServiceRecord readForm() {
        Customer customer = (Customer) customerCombo.getSelectedItem();
        if (customer == null) {
            UIHelper.showError(this, "Add at least one customer first.");
            return null;
        }
        String bike = bikeField.getText().trim();
        if (bike.isEmpty()) {
            UIHelper.showError(this, "Bike model is required.");
            return null;
        }
        ServiceRecord r = new ServiceRecord();
        r.setId(selectedId);
        r.setCustomerId(customer.getId());
        r.setBikeModel(bike);
        r.setServiceType((String) typeCombo.getSelectedItem());
        r.setStatus((String) statusCombo.getSelectedItem());
        try {
            r.setAmount(Double.parseDouble(amountField.getText().trim()));
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Enter a valid amount.");
            return null;
        }
        try {
            r.setServiceDate(LocalDate.parse(dateField.getText().trim()));
        } catch (DateTimeParseException ex) {
            UIHelper.showError(this, "Date must be YYYY-MM-DD.");
            return null;
        }
        r.setNotes(notesField.getText().trim());
        return r;
    }

    private void addRecord() {
        ServiceRecord r = readForm();
        if (r == null) {
            return;
        }
        try {
            ServiceDAO.insert(r);
            UIHelper.showInfo(this, "Service record added.");
            clearForm();
            reload();
            notifyChange();
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void updateRecord() {
        if (selectedId < 0) {
            UIHelper.showError(this, "Select a service record first.");
            return;
        }
        ServiceRecord r = readForm();
        if (r == null) {
            return;
        }
        r.setId(selectedId);
        try {
            ServiceDAO.update(r);
            UIHelper.showInfo(this, "Service record updated.");
            reload();
            notifyChange();
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void deleteRecord() {
        if (selectedId < 0) {
            UIHelper.showError(this, "Select a record to delete.");
            return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Delete this service record?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ServiceDAO.delete(selectedId);
            UIHelper.showInfo(this, "Record deleted.");
            clearForm();
            reload();
            notifyChange();
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void notifyChange() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
