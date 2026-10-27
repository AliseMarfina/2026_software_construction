package com.calorietracker.backend.product;

import com.calorietracker.backend.common.BadRequestException;
import com.calorietracker.backend.common.ExternalServiceUnavailableException;
import com.calorietracker.backend.product.dto.CreateCustomProductRequest;
import com.calorietracker.backend.product.dto.ProductResponse;
import com.calorietracker.backend.product.off.OffProduct;
import com.calorietracker.backend.product.off.OffProperties;
import com.calorietracker.backend.product.off.OpenFoodFactsClient;
import com.calorietracker.backend.product.off.SlidingWindowRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int SEARCH_LIMIT = 20;

    private final ProductRepository productRepository;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final SlidingWindowRateLimiter offRateLimiter;
    private final OffProperties offProperties;

    public List<ProductResponse> search(String query, Long userId) {
        String trimmed = query.trim();
        if (trimmed.length() < MIN_QUERY_LENGTH) {
            throw new BadRequestException("Запрос должен содержать не менее " + MIN_QUERY_LENGTH + " символов");
        }

        List<Product> found = productRepository.search(escapeLikePattern(trimmed), userId, SEARCH_LIMIT);
        if (found.size() < offProperties.minDbResults()) {
            found = supplementFromOpenFoodFacts(trimmed, found);
        }
        return found.stream().map(ProductResponse::from).toList();
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

    private List<Product> supplementFromOpenFoodFacts(String query, List<Product> found) {
        if (!offRateLimiter.tryAcquire()) {
            log.warn("Лимит запросов к Open Food Facts исчерпан, поиск только по БД: {}", query);
            return requireNonEmpty(found);
        }

        List<OffProduct> imported;
        try {
            log.info("Запрос к Open Food Facts: {}", query);
            imported = openFoodFactsClient.search(query);
        } catch (RestClientException e) {
            log.warn("Open Food Facts недоступен: {}", e.getMessage());
            return requireNonEmpty(found);
        }

        if (imported.isEmpty()) {
            return found;
        }

        imported.forEach(product -> productRepository.insertIfAbsent(
                product.name(), product.kcal(), product.protein(), product.fat(), product.carbs(), product.barcode()));
        List<String> barcodes = imported.stream().map(OffProduct::barcode).toList();
        return mergeById(found, productRepository.findByBarcodeIn(barcodes));
    }

    private List<Product> requireNonEmpty(List<Product> found) {
        if (found.isEmpty()) {
            throw new ExternalServiceUnavailableException("Внешняя база продуктов недоступна, повторите запрос позже");
        }
        return found;
    }

    private List<Product> mergeById(List<Product> first, List<Product> second) {
        Map<Long, Product> byId = new LinkedHashMap<>();
        first.forEach(product -> byId.put(product.getId(), product));
        second.forEach(product -> byId.putIfAbsent(product.getId(), product));
        return byId.values().stream()
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(SEARCH_LIMIT)
                .toList();
    }

    private String escapeLikePattern(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}