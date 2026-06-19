package com.dashboard.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Plain data holder for one row of the `sales` table.
 * Kept dependency-free so it can be reused anywhere in the pipeline.
 */
public class SaleRecord {

    private final int id;
    private final String productName;
    private final String category;
    private final String region;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final LocalDate saleDate;

    public SaleRecord(int id, String productName, String category, String region,
                       int quantity, BigDecimal unitPrice, LocalDate saleDate) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.region = region;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.saleDate = saleDate;
    }

    public int getId() { return id; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public String getRegion() { return region; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public LocalDate getSaleDate() { return saleDate; }

    /** Revenue for this single row = quantity * unitPrice. */
    public BigDecimal getRevenue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
