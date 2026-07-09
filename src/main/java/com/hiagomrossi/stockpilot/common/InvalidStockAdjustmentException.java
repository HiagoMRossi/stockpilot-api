package com.hiagomrossi.stockpilot.common;

public class InvalidStockAdjustmentException extends RuntimeException {

    public InvalidStockAdjustmentException(String message) {
        super(message);
    }
}
