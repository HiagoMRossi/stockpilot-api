package com.hiagomrossi.stockpilot.product.service;

import com.hiagomrossi.stockpilot.common.DuplicateSkuException;
import com.hiagomrossi.stockpilot.common.ProductNotFoundException;
import com.hiagomrossi.stockpilot.product.ProductEntity;
import com.hiagomrossi.stockpilot.product.ProductRepository;
import com.hiagomrossi.stockpilot.product.dto.ProductRequest;
import com.hiagomrossi.stockpilot.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        ProductRequest request = new ProductRequest("Mouse Gamer", "MOU-001", 10);

        ProductEntity savedEntity = new ProductEntity(1L, "Mouse Gamer", "MOU-001", 10);

        when(productRepository.findBySku("MOU-001")).thenReturn(Optional.empty());
        when(productRepository.save(ArgumentMatchers.any(ProductEntity.class))).thenReturn(savedEntity);

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Mouse Gamer", response.getName());
        assertEquals("MOU-001", response.getSku());
        assertEquals(10, response.getQuantity());
    }

    @Test
    void shouldThrowDuplicateSkuExceptionWhenCreatingProductWithExistingSku() {
        ProductRequest request = new ProductRequest("Mouse Gamer", "MOU-001", 10);

        ProductEntity existingEntity = new ProductEntity(1L, "Outro Produto", "MOU-001", 5);

        when(productRepository.findBySku("MOU-001")).thenReturn(Optional.of(existingEntity));

        assertThrows(DuplicateSkuException.class, () -> productService.create(request));

        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenFindingProductByIdThatDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(999L));
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenDeletingProductThatDoesNotExist() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteById(999L));

        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldThrowDuplicateSkuExceptionWhenUpdatingProductWithSkuFromAnotherProduct() {
        ProductRequest request = new ProductRequest("Webcam HD", "MON-001", 9);

        ProductEntity currentProduct = new ProductEntity(2L, "Webcam", "WEB-001", 7);
        ProductEntity existingProductWithSameSku = new ProductEntity(1L, "Monitor", "MON-001", 5);

        when(productRepository.findById(2L)).thenReturn(Optional.of(currentProduct));
        when(productRepository.findBySku("MON-001")).thenReturn(Optional.of(existingProductWithSameSku));

        assertThrows(DuplicateSkuException.class, () -> productService.update(2L, request));

        verify(productRepository, never()).save(any(ProductEntity.class));
    }
}