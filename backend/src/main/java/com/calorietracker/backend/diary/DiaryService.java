package com.calorietracker.backend.diary;

import com.calorietracker.backend.common.ForbiddenException;
import com.calorietracker.backend.common.NotFoundException;
import com.calorietracker.backend.diary.dto.CreateDiaryEntryRequest;
import com.calorietracker.backend.diary.dto.DaySummaryResponse;
import com.calorietracker.backend.diary.dto.DiaryEntryResponse;
import com.calorietracker.backend.product.Product;
import com.calorietracker.backend.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final DiaryEntryRepository diaryEntryRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    @Transactional
    public DiaryEntryResponse addEntry(CreateDiaryEntryRequest request, Long userId) {
        Product product = productRepository.findVisibleById(request.productId(), userId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));
        BigDecimal grams = request.grams();

        DiaryEntry entry = new DiaryEntry();
        entry.setUserId(userId);
        entry.setProductId(product.getId());
        entry.setProductName(product.getName());
        entry.setGrams(grams);
        entry.setKcal(portion(product.getKcalPer100g(), grams));
        entry.setProtein(portion(product.getProteinPer100g(), grams));
        entry.setFat(portion(product.getFatPer100g(), grams));
        entry.setCarbs(portion(product.getCarbsPer100g(), grams));
        entry.setLoggedDate(LocalDate.now(clock));

        return DiaryEntryResponse.from(diaryEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public DaySummaryResponse getDay(LocalDate date, Long userId) {
        List<DiaryEntryResponse> entries = diaryEntryRepository
                .findByUserIdAndLoggedDateOrderByIdAsc(userId, date).stream()
                .map(DiaryEntryResponse::from)
                .toList();
        return DaySummaryResponse.of(date, entries);
    }

    @Transactional
    public void deleteEntry(Long id, Long userId) {
        DiaryEntry entry = diaryEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        if (!entry.getUserId().equals(userId)) {
            throw new ForbiddenException("Запись принадлежит другому пользователю");
        }
        diaryEntryRepository.delete(entry);
    }

    private BigDecimal portion(BigDecimal per100g, BigDecimal grams) {
        return per100g.multiply(grams).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}