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

    @Transactional
    public Product deductStock(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not available: " + product.getName());
        }
        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock for " + product.getName()
                    + ". Available: " + product.getStock());
        }

        product.setStock(product.getStock() - quantity);
        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product restoreStock(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setStock(product.getStock() + quantity);
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK && product.getStock() > 0) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        return productRepository.save(product);
    }
}
