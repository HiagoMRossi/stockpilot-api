package com.hiagomrossi.stockpilot.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiagomrossi.stockpilot.StockpilotApplication;
import com.hiagomrossi.stockpilot.common.DuplicateSkuException;
import com.hiagomrossi.stockpilot.common.InvalidStockAdjustmentException;
import com.hiagomrossi.stockpilot.common.ProductNotFoundException;
import com.hiagomrossi.stockpilot.product.dto.ProductRequest;
import com.hiagomrossi.stockpilot.product.dto.ProductResponse;
import com.hiagomrossi.stockpilot.product.dto.StockAdjustmentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = StockpilotApplication.class)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void shouldReturnAllProducts() throws Exception {
        List<ProductResponse> products = List.of(
                productResponse(1L, "Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5),
                productResponse(2L, "Teclado", "KEY-001", 20, 299.90, "Peripherals", 5)
        );

        when(productService.findAll(isNull(), any(Pageable.class))).thenReturn(new PageImpl<>(products));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Mouse Gamer"))
                .andExpect(jsonPath("$.content[0].price").value(199.90))
                .andExpect(jsonPath("$.content[1].sku").value("KEY-001"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldSearchProductsByNameOrSku() throws Exception {
        List<ProductResponse> products = List.of(
                productResponse(1L, "Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5)
        );

        when(productService.findAll(eq("mouse"), any(Pageable.class))).thenReturn(new PageImpl<>(products));

        mockMvc.perform(get("/api/v1/products")
                        .param("search", "mouse")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("MOU-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        ProductResponse product = productResponse(1L, "Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);

        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mouse Gamer"))
                .andExpect(jsonPath("$.sku").value("MOU-001"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.price").value(199.90))
                .andExpect(jsonPath("$.category").value("Peripherals"))
                .andExpect(jsonPath("$.lowStock").value(false));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(productService.findById(999L))
                .thenThrow(new ProductNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found with id: 999"));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest request = productRequest("Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);
        ProductResponse response = productResponse(1L, "Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);

        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mouse Gamer"))
                .andExpect(jsonPath("$.sku").value("MOU-001"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.price").value(199.90))
                .andExpect(jsonPath("$.category").value("Peripherals"));
    }

    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        ProductRequest request = productRequest("", "", -1, -1.0, "", -1);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn409WhenCreatingProductWithDuplicateSku() throws Exception {
        ProductRequest request = productRequest("Mouse Gamer", "MOU-001", 10, 199.90, "Peripherals", 5);

        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new DuplicateSkuException("Product with sku already exists: MOU-001"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Product with sku already exists: MOU-001"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        ProductRequest request = productRequest("Mouse Gamer RGB", "MOU-001-RGB", 15, 249.90, "Peripherals", 4);
        ProductResponse response = productResponse(1L, "Mouse Gamer RGB", "MOU-001-RGB", 15, 249.90, "Peripherals", 4);

        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mouse Gamer RGB"))
                .andExpect(jsonPath("$.sku").value("MOU-001-RGB"))
                .andExpect(jsonPath("$.quantity").value(15))
                .andExpect(jsonPath("$.price").value(249.90));
    }

    @Test
    void shouldReturnLowStockProducts() throws Exception {
        List<ProductResponse> products = List.of(
                productResponse(1L, "Mouse Gamer", "MOU-001", 3, 199.90, "Peripherals", 5)
        );

        when(productService.findLowStockProducts()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("MOU-001"))
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }

    @Test
    void shouldAdjustStock() throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest(-3);
        ProductResponse response = productResponse(1L, "Mouse Gamer", "MOU-001", 7, 199.90, "Peripherals", 5);

        when(productService.adjustStock(eq(1L), any(StockAdjustmentRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7))
                .andExpect(jsonPath("$.lowStock").value(false));
    }

    @Test
    void shouldReturn400WhenStockAdjustmentMakesQuantityNegative() throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest(-50);

        when(productService.adjustStock(eq(1L), any(StockAdjustmentRequest.class)))
                .thenThrow(new InvalidStockAdjustmentException("Stock adjustment cannot make quantity negative"));

        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Stock adjustment cannot make quantity negative"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingProductThatDoesNotExist() throws Exception {
        doThrow(new ProductNotFoundException("Product not found with id: 999"))
                .when(productService).deleteById(999L);

        mockMvc.perform(delete("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found with id: 999"));
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

    private ProductResponse productResponse(
            Long id,
            String name,
            String sku,
            Integer quantity,
            double price,
            String category,
            Integer lowStockThreshold
    ) {
        return new ProductResponse(
                id,
                name,
                sku,
                quantity,
                BigDecimal.valueOf(price),
                category,
                lowStockThreshold,
                quantity <= lowStockThreshold
        );
    }
}
