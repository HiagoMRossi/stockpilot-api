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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.List;
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
        ProductRequest request = productRequest("Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);

        ProductEntity savedEntity = productEntity(1L, "Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);

        when(productRepository.findBySku("MOU-001")).thenReturn(Optional.empty());
        when(productRepository.save(ArgumentMatchers.any(ProductEntity.class))).thenReturn(savedEntity);

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Mouse Gamer", response.getName());
        assertEquals("MOU-001", response.getSku());
        assertEquals(10, response.getQuantity());
        assertEquals(0, new BigDecimal("199.90").compareTo(response.getPrice()));
        assertEquals("Peripherals", response.getCategory());
        assertEquals(5, response.getLowStockThreshold());
        assertFalse(response.isLowStock());
    }

    @Test
    void shouldThrowDuplicateSkuExceptionWhenCreatingProductWithExistingSku() {
        ProductRequest request = productRequest("Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);

        ProductEntity existingEntity = productEntity(1L, "Outro Produto", "MOU-001", 5, 99.90, "Other", 5);

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
        ProductRequest request = productRequest("Webcam HD", "MON-001", 9, 249.90, "Cameras", 3);

        ProductEntity currentProduct = productEntity(2L, "Webcam", "WEB-001", 7, 199.90, "Cameras", 3);
        ProductEntity existingProductWithSameSku = productEntity(1L, "Monitor", "MON-001", 5, 899.90, "Displays", 2);

        when(productRepository.findById(2L)).thenReturn(Optional.of(currentProduct));
        when(productRepository.findBySku("MON-001")).thenReturn(Optional.of(existingProductWithSameSku));

        assertThrows(DuplicateSkuException.class, () -> productService.update(2L, request));

        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void shouldSearchProductsByNameOrSkuWithPagination() {
        PageRequest pageable = PageRequest.of(0, 10);
        ProductEntity product = productEntity(1L, "Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);

        when(productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                "mouse",
                "mouse",
                pageable
        )).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        Page<ProductResponse> response = productService.findAll("mouse", pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("Mouse Gamer", response.getContent().getFirst().getName());
    }

    @Test
    void shouldReturnLowStockProducts() {
        ProductEntity lowStockProduct = productEntity(1L, "Mouse Gamer", "MOU-001", 3, 199.90, "Peripherals", 5);
        ProductEntity healthyStockProduct = productEntity(2L, "Keyboard", "KEY-001", 20, 299.90, "Peripherals", 5);

        when(productRepository.findAll()).thenReturn(List.of(lowStockProduct, healthyStockProduct));

        List<ProductResponse> response = productService.findLowStockProducts();

        assertEquals(1, response.size());
        assertEquals("MOU-001", response.getFirst().getSku());
        assertTrue(response.getFirst().isLowStock());
    }

    @Test
    void shouldAdjustStockByQuantityChange() {
        ProductEntity product = productEntity(1L, "Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);
        ProductEntity savedProduct = productEntity(1L, "Mouse Gamer", "MOU-001", 7, 199.90, "Peripherals", 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(savedProduct);

        ProductResponse response = productService.adjustStock(1L, new StockAdjustmentRequest(-3));

        assertEquals(7, response.getQuantity());
        assertFalse(response.isLowStock());
    }

    @Test
    void shouldThrowExceptionWhenStockAdjustmentMakesQuantityNegative() {
        ProductEntity product = productEntity(1L, "Mouse Gamer", "MOU-001", 2, 199.90, "Peripherals", 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(
                InvalidStockAdjustmentException.class,
                () -> productService.adjustStock(1L, new StockAdjustmentRequest(-3))
        );

        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    private ProductRequest productRequest(
            String name,
            String sku,
            Integer quantity,
            double price,
            String category,
            Integer lowStockThreshold
    ) {
        return new ProductRequest(
                name,
                sku,
                quantity,
                BigDecimal.valueOf(price),
                category,
                lowStockThreshold
        );
    }

    private ProductEntity productEntity(
            Long id,
            String name,
            String sku,
            Integer quantity,
            double price,
            String category,
            Integer lowStockThreshold
    ) {
        return new ProductEntity(
                id,
                name,
                sku,
                quantity,
                BigDecimal.valueOf(price),
                category,
                lowStockThreshold
        );
    }
}
