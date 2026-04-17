package com.hiagomrossi.stockpilot.product.service;

import com.hiagomrossi.stockpilot.product.dto.ProductRequest;
import com.hiagomrossi.stockpilot.product.dto.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final List<ProductResponse> products = new ArrayList<>();
    private Long nextId = 1L;

    public List<ProductResponse> findAll() {
        return products;
    }

    public ProductResponse create(ProductRequest request) {
        ProductResponse product = new ProductResponse(
                nextId++,
                request.getName(),
                request.getSku(),
                request.getQuantity()
        );

        products.add(product);
        return product;
    }

    public ProductResponse findById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}