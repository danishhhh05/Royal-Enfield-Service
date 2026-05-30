package com.royalenfield.service;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

/**
 * Customer management — search, table, CRUD form.
 */
public class CustomerPanel extends JPanel {

    private final Runnable onDataChanged;
    private final DefaultTableModel tableModel = UIHelper.tableModel(
            new String[]{"ID", "Name", "Phone", "Email", "Address"});
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = UIHelper.textField("Search by name, phone, email...");
    private final JTextField nameField = UIHelper.textField("Full name");
    private final JTextField phoneField = UIHelper.textField("Phone number");
    private final JTextField emailField = UIHelper.textField("Email");
    private final JTextField addressField = UIHelper.textField("Address");

    private int selectedId = -1;

    public CustomerPanel(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadTable("");
    }

    private JPanel buildToolbar() {
        Dashboard.RoundedPanel bar = new Dashboard.RoundedPanel(AppColors.SURFACE, UIHelper.CORNER);
        bar.setLayout(new BorderLayout(12, 0));
        bar.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel heading = UIHelper.title("Customers");
        bar.add(heading, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchField.setPreferredSize(new Dimension(280, 38));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loadTable(searchField.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loadTable(searchField.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                loadTable(searchField.getText());
            }
        });
        JButton searchBtn = UIHelper.secondaryButton("Search");
        searchBtn.addActionListener(e -> loadTable(searchField.getText()));
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
        formCard.setPreferredSize(new Dimension(300, 0));
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
        form.add(UIHelper.title("Customer Details"));
        form.add(UIHelper.formRow("Name *", nameField));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Phone", phoneField));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Email", emailField));
        form.add(Box.createVerticalStrut(8));
        form.add(UIHelper.formRow("Address", addressField));
        form.add(Box.createVerticalStrut(16));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        JButton add = UIHelper.primaryButton("Add");
        JButton update = UIHelper.secondaryButton("Update");
        JButton delete = UIHelper.dangerButton("Delete");
        JButton clear = UIHelper.secondaryButton("Clear");
        add.addActionListener(e -> addCustomer());
        update.addActionListener(e -> updateCustomer());
        delete.addActionListener(e -> deleteCustomer());
        clear.addActionListener(e -> clearForm());
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(clear);
        form.add(buttons);
        return form;
    }

    public void clearForm() {
        selectedId = -1;
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        table.clearSelection();
    }

    public void focusNewCustomer() {
        clearForm();
        nameField.requestFocusInWindow();
    }

    private void fillForm(int row) {
        selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        phoneField.setText(nullSafe(tableModel.getValueAt(row, 2)));
        emailField.setText(nullSafe(tableModel.getValueAt(row, 3)));
        addressField.setText(nullSafe(tableModel.getValueAt(row, 4)));
    }

    private String nullSafe(Object v) {
        return v == null ? "" : v.toString();
    }

    public void loadTablePublic() {
        loadTable(searchField.getText());
    }

    private void loadTable(String query) {
        if (!DBConnection.isConnected()) {
            tableModel.setRowCount(0);
            return;
        }
        try {
            List<Customer> customers = CustomerDAO.search(query);
            tableModel.setRowCount(0);
            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                        c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress()
                });
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "Failed to load customers: " + ex.getMessage());
        }
    }

    private Customer readForm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            UIHelper.showError(this, "Customer name is required.");
            return null;
        }
        Customer c = new Customer();
        c.setId(selectedId);
        c.setName(name);
        c.setPhone(phoneField.getText().trim());
        c.setEmail(emailField.getText().trim());
        c.setAddress(addressField.getText().trim());
        return c;
    }

    private void addCustomer() {
        Customer c = readForm();
        if (c == null) {
            return;
        }
        try {
            CustomerDAO.insert(c);
            UIHelper.showInfo(this, "Customer added successfully.");
            clearForm();
            loadTable(searchField.getText());
            notifyChange();
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void updateCustomer() {
        if (selectedId < 0) {
            UIHelper.showError(this, "Select a customer from the table first.");
            return;
        }
        Customer c = readForm();
        if (c == null) {
            return;
        }
        c.setId(selectedId);
        try {
            CustomerDAO.update(c);
            UIHelper.showInfo(this, "Customer updated.");
            loadTable(searchField.getText());
            notifyChange();
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void deleteCustomer() {
        if (selectedId < 0) {
            UIHelper.showError(this, "Select a customer to delete.");
            return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Delete this customer and all linked service records?",
                "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try {
            CustomerDAO.delete(selectedId);
            UIHelper.showInfo(this, "Customer deleted.");
            clearForm();
            loadTable(searchField.getText());
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
