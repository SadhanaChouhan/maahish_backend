package com.maahish.catalog.entity;

import com.maahish.common.exception.BadRequestException;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.catalog.service.ProductStockService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductStockServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductStockService productStockService;

    @Test
    void deductStock_insufficientStock_throws() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(1).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productStockService.deductStock(1L, 2));
        verify(productRepository, never()).save(any());
    }

    @Test
    void deductStock_success_reducesQuantity() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(5).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productStockService.deductStock(1L, 2);

        assertEquals(3, updated.getStock());
        verify(productRepository).findByIdForUpdate(1L);
        verify(productRepository).save(product);
    }
}
