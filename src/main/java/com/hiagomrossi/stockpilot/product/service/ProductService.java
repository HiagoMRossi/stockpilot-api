package com.hiagomrossi.stockpilot.product.service;

import com.hiagomrossi.stockpilot.common.DuplicateSkuException;
import com.hiagomrossi.stockpilot.common.ProductNotFoundException;
import com.hiagomrossi.stockpilot.product.ProductEntity;
import com.hiagomrossi.stockpilot.product.ProductRepository;
import com.hiagomrossi.stockpilot.product.dto.ProductRequest;
import com.hiagomrossi.stockpilot.product.dto.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
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

        ProductEntity updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }

    private ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getQuantity()
        );
    }
}