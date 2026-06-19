package com.dashboard.db;

import com.dashboard.model.SaleRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object: the only class that issues SQL.
 * Returns plain Java objects so the rest of the app never touches JDBC types.
 */
public class SalesDAO {

    private static final String SELECT_ALL_SQL =
            "SELECT id, product_name, category, region, quantity, unit_price, sale_date " +
            "FROM sales ORDER BY sale_date ASC";

    /**
     * Fetches every sales row in one query. For larger datasets you'd add
     * pagination or filtering, but a single bulk read is enough here and
     * avoids the N+1 query problem (we never query per-row).
     */
    public List<SaleRecord> findAll() throws SQLException {
        List<SaleRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                SaleRecord record = new SaleRecord(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getString("region"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("unit_price"),
                        rs.getDate("sale_date").toLocalDate()
                );
                records.add(record);
            }
        }
        return records;
    }
}
