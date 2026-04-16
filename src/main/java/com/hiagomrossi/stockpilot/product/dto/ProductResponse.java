package com.hiagomrossi.stockpilot.product.dto;

public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private Integer quantity;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, String sku, Integer quantity) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
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
}