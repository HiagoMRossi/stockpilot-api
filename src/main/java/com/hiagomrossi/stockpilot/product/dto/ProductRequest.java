package com.hiagomrossi.stockpilot.product.dto;

public class ProductRequest {

    private String name;
    private String sku;
    private Integer quantity;

    public ProductRequest() {
    }

    public ProductRequest(String name, String sku, Integer quantity) {
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
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
}