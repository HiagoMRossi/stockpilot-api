package com.hiagomrossi.stockpilot.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiagomrossi.stockpilot.StockpilotApplication;
import com.hiagomrossi.stockpilot.common.DuplicateSkuException;
import com.hiagomrossi.stockpilot.common.ProductNotFoundException;
import com.hiagomrossi.stockpilot.product.dto.ProductRequest;
import com.hiagomrossi.stockpilot.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                new ProductResponse(1L, "Mouse Gamer", "MOU-001", 10),
                new ProductResponse(2L, "Teclado", "KEY-001", 20)
        );

        when(productService.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mouse Gamer"))
                .andExpect(jsonPath("$[1].sku").value("KEY-001"));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        ProductResponse product = new ProductResponse(1L, "Mouse Gamer", "MOU-001", 10);

        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mouse Gamer"))
                .andExpect(jsonPath("$.sku").value("MOU-001"))
                .andExpect(jsonPath("$.quantity").value(10));
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
        ProductRequest request = new ProductRequest("Mouse Gamer", "MOU-001", 10);
        ProductResponse response = new ProductResponse(1L, "Mouse Gamer", "MOU-001", 10);

        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mouse Gamer"))
                .andExpect(jsonPath("$.sku").value("MOU-001"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        ProductRequest request = new ProductRequest("", "", -1);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn409WhenCreatingProductWithDuplicateSku() throws Exception {
        ProductRequest request = new ProductRequest("Mouse Gamer", "MOU-001", 10);

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
        ProductRequest request = new ProductRequest("Mouse Gamer RGB", "MOU-001-RGB", 15);
        ProductResponse response = new ProductResponse(1L, "Mouse Gamer RGB", "MOU-001-RGB", 15);

        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mouse Gamer RGB"))
                .andExpect(jsonPath("$.sku").value("MOU-001-RGB"))
                .andExpect(jsonPath("$.quantity").value(15));
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
}