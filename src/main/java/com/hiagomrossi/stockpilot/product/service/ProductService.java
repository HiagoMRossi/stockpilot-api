package com.hiagomrossi.stockpilot.product.service;

import com.hiagomrossi.stockpilot.common.DuplicateSkuException;
import com.hiagomrossi.stockpilot.common.InvalidStockAdjustmentException;
import com.hiagomrossi.stockpilot.common.ProductNotFoundException;
import com.hiagomrossi.stockpilot.product.ProductEntity;
import com.hiagomrossi.stockpilot.product.ProductRepository;
import com.hiagomrossi.stockpilot.product.dto.ProductRequest;
import com.hiagomrossi.stockpilot.product.dto.ProductResponse;
import com.hiagomrossi.stockpilot.product.dto.StockAdjustmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponse> findAll(String search, Pageable pageable) {
        Page<ProductEntity> products;

        if (search == null || search.isBlank()) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                    search,
                    search,
                    pageable
            );
        }

        return products.map(this::toResponse);
    }

    public List<ProductResponse> findLowStockProducts() {
        return productRepository.findAll()
                .stream()
                .filter(this::isLowStock)
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        return toResponse(product);
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new DuplicateSkuException("Product with sku already exists: " + request.getSku());
        }

        ProductEntity product = new ProductEntity();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setQuantity(request.getQuantity());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setLowStockThreshold(request.getLowStockThreshold());

        ProductEntity savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        productRepository.findBySku(request.getSku())
                .ifPresent(existingProduct -> {
                    if (!existingProduct.getId().equals(id)) {
                        throw new DuplicateSkuException("Product with sku already exists: " + request.getSku());
                    }
                });

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setQuantity(request.getQuantity());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setLowStockThreshold(request.getLowStockThreshold());

        ProductEntity updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }

    public ProductResponse adjustStock(Long id, StockAdjustmentRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        int adjustedQuantity = product.getQuantity() + request.getQuantityChange();

        if (adjustedQuantity < 0) {
            throw new InvalidStockAdjustmentException("Stock adjustment cannot make quantity negative");
        }

        product.setQuantity(adjustedQuantity);
        ProductEntity updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }

    private ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getQuantity(),
                product.getPrice(),
                product.getCategory(),
                product.getLowStockThreshold(),
                isLowStock(product)
        );
    }

    private boolean isLowStock(ProductEntity product) {
        return product.getQuantity() <= product.getLowStockThreshold();
    }
}
