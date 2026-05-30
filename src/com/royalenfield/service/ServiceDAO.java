package com.royalenfield.service;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ServiceDAO {

    private ServiceDAO() {
    }

    public static List<ServiceRecord> findAll() throws SQLException {
        return findAll("");
    }

    public static List<ServiceRecord> findAll(String search) throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        String sql = """
                SELECT s.id, s.customer_id, c.name AS customer_name,
                       s.bike_model, s.service_type, s.status, s.amount,
                       s.service_date, s.notes
                FROM service_records s
                LEFT JOIN customers c ON c.id = s.customer_id
                WHERE c.name LIKE ? OR s.bike_model LIKE ? OR s.service_type LIKE ?
                   OR s.status LIKE ? OR s.notes LIKE ?
                ORDER BY s.service_date DESC, s.id DESC
                """;
        try (PreparedStatement ps = DBConnection.prepare(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public static int insert(ServiceRecord r) throws SQLException {
        String sql = """
                INSERT INTO service_records
                (customer_id, bike_model, service_type, status, amount, service_date, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, r);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public static void update(ServiceRecord r) throws SQLException {
        String sql = """
                UPDATE service_records SET customer_id=?, bike_model=?, service_type=?,
                status=?, amount=?, service_date=?, notes=? WHERE id=?
                """;
        try (PreparedStatement ps = DBConnection.prepare(sql)) {
            bind(ps, r);
            ps.setInt(8, r.getId());
            ps.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.prepare("DELETE FROM service_records WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static void bind(PreparedStatement ps, ServiceRecord r) throws SQLException {
        ps.setInt(1, r.getCustomerId());
        ps.setString(2, r.getBikeModel());
        ps.setString(3, r.getServiceType());
        ps.setString(4, r.getStatus());
        ps.setDouble(5, r.getAmount());
        LocalDate d = r.getServiceDate() != null ? r.getServiceDate() : LocalDate.now();
        ps.setDate(6, Date.valueOf(d));
        ps.setString(7, r.getNotes() != null ? r.getNotes() : "");
    }

    private static ServiceRecord map(ResultSet rs) throws SQLException {
        ServiceRecord r = new ServiceRecord();
        r.setId(rs.getInt("id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setBikeModel(rs.getString("bike_model"));
        r.setServiceType(rs.getString("service_type"));
        r.setStatus(rs.getString("status"));
        r.setAmount(rs.getDouble("amount"));
        Date d = rs.getDate("service_date");
        if (d != null) {
            r.setServiceDate(d.toLocalDate());
        }
        r.setNotes(rs.getString("notes"));
        return r;
    }
}
