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
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(1).reservedStock(0).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productStockService.deductStock(1L, 2));
        verify(productRepository, never()).save(any());
    }

    @Test
    void deductStock_success_reducesQuantity() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(5).reservedStock(0).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productStockService.deductStock(1L, 2);

        assertEquals(3, updated.getStock());
        assertEquals(ProductStatus.ACTIVE, updated.getStatus());
        verify(productRepository).findByIdForUpdate(1L);
        verify(productRepository).save(product);
    }

    @Test
    void deductStock_toZero_keepsActiveStatus() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(1).reservedStock(0).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productStockService.deductStock(1L, 1);

        assertEquals(0, updated.getStock());
        assertEquals(ProductStatus.ACTIVE, updated.getStatus());
    }

    @Test
    void reserveStock_incrementsReserved_notPhysicalStock() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(5).reservedStock(1).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productStockService.reserveStock(1L, 2);

        assertEquals(5, updated.getStock());
        assertEquals(3, updated.getReservedStock());
        assertEquals(2, ProductStockService.availableStock(updated));
    }

    @Test
    void reserveStock_insufficientAvailable_throws() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(2).reservedStock(2).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productStockService.reserveStock(1L, 1));
    }

    @Test
    void fulfillReservedStock_reducesBothStockAndReserved() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(5).reservedStock(2).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productStockService.fulfillReservedStock(1L, 2);

        assertEquals(3, updated.getStock());
        assertEquals(0, updated.getReservedStock());
    }

    @Test
    void releaseReservation_decrementsReservedOnly() {
        Product product = Product.builder()
                .id(1L).name("Saree").status(ProductStatus.ACTIVE).stock(5).reservedStock(3).build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product updated = productStockService.releaseReservation(1L, 2);

        assertEquals(5, updated.getStock());
        assertEquals(1, updated.getReservedStock());
    }
}
