package com.calorietracker.backend.product;

import com.calorietracker.backend.common.BadRequestException;
import com.calorietracker.backend.product.dto.CreateCustomProductRequest;
import com.calorietracker.backend.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int SEARCH_LIMIT = 20;

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String query, Long userId) {
        String trimmed = query.trim();
        if (trimmed.length() < MIN_QUERY_LENGTH) {
            throw new BadRequestException("Запрос должен содержать не менее " + MIN_QUERY_LENGTH + " символов");
        }
        return productRepository.search(escapeLikePattern(trimmed), userId, SEARCH_LIMIT).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse createCustom(CreateCustomProductRequest request, Long userId) {
        Product product = new Product();
        product.setName(request.name().trim());
        product.setKcalPer100g(request.kcalPer100g());
        product.setProteinPer100g(request.proteinPer100g());
        product.setFatPer100g(request.fatPer100g());
        product.setCarbsPer100g(request.carbsPer100g());
        product.setOwnerId(userId);
        return ProductResponse.from(productRepository.save(product));
    }

    private String escapeLikePattern(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}