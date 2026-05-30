package com.royalenfield.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class CustomerDAO {

    private CustomerDAO() {
    }

    public static List<Customer> findAll() throws SQLException {
        return search("");
    }

    public static List<Customer> search(String query) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = """
                SELECT id, name, phone, email, address FROM customers
                WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? OR address LIKE ?
                ORDER BY name
                """;
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        try (PreparedStatement ps = DBConnection.prepare(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public static Customer findById(int id) throws SQLException {
        String sql = "SELECT id, name, phone, email, address FROM customers WHERE id = ?";
        try (PreparedStatement ps = DBConnection.prepare(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public static int insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public static void update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, address=? WHERE id=?";
        try (PreparedStatement ps = DBConnection.prepare(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id=?";
        try (PreparedStatement ps = DBConnection.prepare(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"));
    }
}
