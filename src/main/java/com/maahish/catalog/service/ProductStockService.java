package com.maahish.catalog.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final ProductRepository productRepository;

    public static int availableStock(Product product) {
        int stock = product.getStock() != null ? product.getStock() : 0;
        int reserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
        return Math.max(0, stock - reserved);
    }

    @Transactional
    public Product reserveStock(Long productId, int quantity) {
        Product product = loadForUpdate(productId);
        assertActive(product);
        if (quantity <= 0) {
            throw new BadRequestException("Invalid quantity for stock reservation");
        }
        int available = availableStock(product);
        if (available < quantity) {
            throw new BadRequestException("Insufficient stock for " + product.getName()
                    + ". Available: " + available);
        }
        product.setReservedStock(product.getReservedStock() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product releaseReservation(Long productId, int quantity) {
        if (quantity <= 0) {
            return productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        }
        Product product = loadForUpdate(productId);
        int reserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
        product.setReservedStock(Math.max(0, reserved - quantity));
        return productRepository.save(product);
    }

    @Transactional
    public Product fulfillReservedStock(Long productId, int quantity) {
        Product product = loadForUpdate(productId);
        assertActive(product);
        if (quantity <= 0) {
            throw new BadRequestException("Invalid quantity for stock fulfillment");
        }
        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock for " + product.getName()
                    + ". Available: " + product.getStock());
        }
        int reserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
        product.setReservedStock(Math.max(0, reserved - Math.min(reserved, quantity)));
        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product deductStock(Long productId, int quantity) {
        Product product = loadForUpdate(productId);
        assertActive(product);
        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock for " + product.getName()
                    + ". Available: " + product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product restoreStock(Long productId, int quantity) {
        Product product = loadForUpdate(productId);
        product.setStock(product.getStock() + quantity);
        return productRepository.save(product);
    }

    private Product loadForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void assertActive(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not available: " + product.getName());
        }
    }
}
