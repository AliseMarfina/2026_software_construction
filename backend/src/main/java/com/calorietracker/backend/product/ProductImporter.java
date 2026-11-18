package com.calorietracker.backend.product;

import com.calorietracker.backend.diary.DiaryEntryRepository;
import com.calorietracker.backend.product.off.OffProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductImporter {

    private final ProductRepository productRepository;
    private final DiaryEntryRepository diaryEntryRepository;

    @Transactional
    public void importProduct(OffProduct product) {
        int updated = productRepository.updateNutrientsIfChanged(
                product.barcode(), product.kcal(), product.protein(), product.fat(), product.carbs());
        if (updated > 0) {
            diaryEntryRepository.markProductUpdated(product.barcode());
        }
        productRepository.insertIfAbsent(
                product.name(), product.kcal(), product.protein(), product.fat(), product.carbs(), product.barcode());
    }
}
