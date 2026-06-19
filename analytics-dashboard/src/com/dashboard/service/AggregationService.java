package com.dashboard.service;

import com.dashboard.model.SaleRecord;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Turns the flat list of SaleRecord rows into the aggregated shapes the
 * frontend charts need: revenue by category, revenue by region, and a
 * monthly revenue trend. Everything here runs in memory using Collections
 * (HashMap / TreeMap) instead of issuing separate SQL GROUP BY queries —
 * one JDBC round trip feeds every chart, which is the "minimised redundant
 * API calls" part of the pipeline.
 */
public class AggregationService {

    /** Revenue total per category. HashMap is fine: no ordering requirement. */
    public Map<String, BigDecimal> revenueByCategory(List<SaleRecord> records) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (SaleRecord r : records) {
            totals.merge(r.getCategory(), r.getRevenue(), BigDecimal::add);
        }
        return totals;
    }

    /** Revenue total per region. */
    public Map<String, BigDecimal> revenueByRegion(List<SaleRecord> records) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (SaleRecord r : records) {
            totals.merge(r.getRegion(), r.getRevenue(), BigDecimal::add);
        }
        return totals;
    }

    /**
     * Revenue total per month (YYYY-MM), sorted chronologically.
     * TreeMap keeps keys sorted automatically so the frontend gets the
     * trend line already in date order — no extra sorting step needed.
     */
    public Map<String, BigDecimal> revenueByMonth(List<SaleRecord> records) {
        DateTimeFormatter monthKey = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> totals = new TreeMap<>();
        for (SaleRecord r : records) {
            String key = r.getSaleDate().format(monthKey);
            totals.merge(key, r.getRevenue(), BigDecimal::add);
        }
        return totals;
    }

    /** Units sold per product, used for a "top products" style chart. */
    public Map<String, Integer> unitsSoldByProduct(List<SaleRecord> records) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (SaleRecord r : records) {
            totals.merge(r.getProductName(), r.getQuantity(), Integer::sum);
        }
        return totals;
    }

    /** Grand total revenue across all records — a single summary KPI. */
    public BigDecimal totalRevenue(List<SaleRecord> records) {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleRecord r : records) {
            total = total.add(r.getRevenue());
        }
        return total;
    }
}
