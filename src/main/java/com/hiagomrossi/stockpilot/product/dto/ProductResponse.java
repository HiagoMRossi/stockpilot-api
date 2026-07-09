package com.hiagomrossi.stockpilot.product.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private Integer quantity;
    private BigDecimal price;
    private String category;
    private Integer lowStockThreshold;
    private boolean lowStock;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, String sku, Integer quantity) {
        this(id, name, sku, quantity, BigDecimal.ZERO, "Uncategorized", 5, quantity <= 5);
    }

    public ProductResponse(
            Long id,
            String name,
            String sku,
            Integer quantity,
            BigDecimal price,
            String category,
            Integer lowStockThreshold,
            boolean lowStock
    ) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.lowStockThreshold = lowStockThreshold;
        this.lowStock = lowStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }
}
