package com.calorietracker.backend.product.off;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

@Component
public class OpenFoodFactsClient {

    private static final BigDecimal MAX_KCAL = BigDecimal.valueOf(900);
    private static final BigDecimal MAX_MACRO = BigDecimal.valueOf(100);
    private static final BigDecimal KJ_PER_KCAL = new BigDecimal("4.184");
    private static final int MAX_NAME_LENGTH = 255;

    private final RestClient restClient;
    private final int pageSize;

    public OpenFoodFactsClient(OffProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.timeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
                .requestFactory(requestFactory)
                .build();
        this.pageSize = properties.pageSize();
    }

    /**
     * Ищет продукты по названию. Пропускает продукты с неполными или неправдоподобными данными.
     * При сетевой ошибке, таймауте или неожиданном ответе бросает RestClientException.
     */
    public List<OffProduct> search(String query) {
        String cleaned = sanitizeQuery(query);
        if (cleaned.isBlank()) {
            return List.of();
        }

        OffSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", "{query}")
                        .queryParam("page_size", pageSize)
                        .queryParam("fields", "code,product_name,nutriments")
                        .build(cleaned))
                .retrieve()
                .body(OffSearchResponse.class);

        if (response == null || response.hits() == null) {
            return List.of();
        }
        return response.hits().stream()
                .map(this::toOffProduct)
                .flatMap(Optional::stream)
                .toList();
    }

    private String sanitizeQuery(String query) {
        return query.replaceAll("[\\\\+\\-!(){}\\[\\]^\"~*?:/&|]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Optional<OffProduct> toOffProduct(OffSearchResponse.Item item) {
        if (isBlank(item.code()) || isBlank(item.productName()) || item.nutriments() == null) {
            return Optional.empty();
        }
        OffSearchResponse.Nutriments n = item.nutriments();
        BigDecimal kcal = n.kcal() != null ? n.kcal() : kjToKcal(n.kj());
        if (!inRange(kcal, MAX_KCAL) || !inRange(n.protein(), MAX_MACRO)
                || !inRange(n.fat(), MAX_MACRO) || !inRange(n.carbs(), MAX_MACRO)) {
            return Optional.empty();
        }
        String name = item.productName().trim();
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }
        return Optional.of(new OffProduct(
                item.code().trim(),
                name,
                scale(kcal),
                scale(n.protein()),
                scale(n.fat()),
                scale(n.carbs())
        ));
    }

    private BigDecimal kjToKcal(BigDecimal kj) {
        return kj == null ? null : kj.divide(KJ_PER_KCAL, 2, RoundingMode.HALF_UP);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean inRange(BigDecimal value, BigDecimal max) {
        return value != null && value.signum() >= 0 && value.compareTo(max) <= 0;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}