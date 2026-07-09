package com.hiagomrossi.stockpilot.product.dto;

import jakarta.validation.constraints.NotNull;

public class StockAdjustmentRequest {

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;

    public StockAdjustmentRequest() {
    }

    public StockAdjustmentRequest(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }
}
