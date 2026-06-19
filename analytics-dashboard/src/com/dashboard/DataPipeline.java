package com.dashboard;

import com.dashboard.db.SalesDAO;
import com.dashboard.model.SaleRecord;
import com.dashboard.service.AggregationService;
import com.dashboard.service.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entry point for the data pipeline:
 *   1. Pull all sales rows over JDBC (one query, one round trip).
 *   2. Aggregate them in memory with Collections (HashMap/TreeMap).
 *   3. Serialize the aggregates to a single JSON file the frontend reads.
 *
 * Run this manually whenever the underlying data changes, or trigger it
 * from DashboardServer's /api/refresh endpoint.
 */
public class DataPipeline {

    private static final String OUTPUT_PATH = "output/dashboard-data.json";

    public static void main(String[] args) {
        try {
            run();
            System.out.println("Pipeline finished. JSON written to " + OUTPUT_PATH);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("File write error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void run() throws SQLException, IOException {
        SalesDAO dao = new SalesDAO();
        AggregationService agg = new AggregationService();

        // Step 1: one JDBC call fetches everything the pipeline needs.
        List<SaleRecord> records = dao.findAll();

        // Step 2: aggregate in memory using Collections.
        Map<String, BigDecimal> byCategory = agg.revenueByCategory(records);
        Map<String, BigDecimal> byRegion = agg.revenueByRegion(records);
        Map<String, BigDecimal> byMonth = agg.revenueByMonth(records);
        Map<String, Integer> unitsByProduct = agg.unitsSoldByProduct(records);
        BigDecimal totalRevenue = agg.totalRevenue(records);

        // Step 3: build the JSON document by hand-assembling each section.
        String json = buildJson(records.size(), totalRevenue, byCategory, byRegion, byMonth, unitsByProduct);

        Path outPath = Paths.get(OUTPUT_PATH);
        Files.createDirectories(outPath.getParent());
        Files.write(outPath, json.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildJson(int totalRecords,
                                      BigDecimal totalRevenue,
                                      Map<String, BigDecimal> byCategory,
                                      Map<String, BigDecimal> byRegion,
                                      Map<String, BigDecimal> byMonth,
                                      Map<String, Integer> unitsByProduct) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"totalRecords\": ").append(totalRecords).append(",\n");
        sb.append("  \"totalRevenue\": ").append(totalRevenue.setScale(2, java.math.RoundingMode.HALF_UP)).append(",\n");
        sb.append("  \"revenueByCategory\": ").append(JsonWriter.mapToJson(byCategory)).append(",\n");
        sb.append("  \"revenueByRegion\": ").append(JsonWriter.mapToJson(byRegion)).append(",\n");
        sb.append("  \"revenueByMonth\": ").append(JsonWriter.mapToJson(byMonth)).append(",\n");

        // Top 8 products by units sold, for a manageable bar chart.
        List<Map.Entry<String, Integer>> sortedProducts = new ArrayList<>(unitsByProduct.entrySet());
        sortedProducts.sort((a, b) -> b.getValue() - a.getValue());
        List<Map.Entry<String, Integer>> top = sortedProducts.subList(0, Math.min(8, sortedProducts.size()));

        List<String> topNames = new ArrayList<>();
        List<Integer> topUnits = new ArrayList<>();
        for (Map.Entry<String, Integer> e : top) {
            topNames.add(e.getKey());
            topUnits.add(e.getValue());
        }

        sb.append("  \"topProducts\": {\n");
        sb.append("    \"labels\": ").append(JsonWriter.keysToJsonArray(topNames)).append(",\n");
        sb.append("    \"units\": ").append(JsonWriter.valuesToJsonArray(topUnits)).append("\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }
}
