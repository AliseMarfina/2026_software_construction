package com.calorietracker.backend.diary;

import com.calorietracker.backend.common.BadRequestException;
import com.calorietracker.backend.common.ForbiddenException;
import com.calorietracker.backend.common.NotFoundException;
import com.calorietracker.backend.diary.dto.CreateDiaryEntryRequest;
import com.calorietracker.backend.diary.dto.DaySummaryResponse;
import com.calorietracker.backend.diary.dto.DiaryEntryResponse;
import com.calorietracker.backend.diary.dto.StatsResponse;
import com.calorietracker.backend.diary.dto.UpdateDiaryEntryRequest;
import com.calorietracker.backend.product.Product;
import com.calorietracker.backend.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final long MAX_STATS_DAYS = 366;

    private final DiaryEntryRepository diaryEntryRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    @Transactional
    public DiaryEntryResponse addEntry(CreateDiaryEntryRequest request, Long userId) {
        Product product = productRepository.findVisibleById(request.productId(), userId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));
        Instant eatenAt = request.eatenAt() == null ? null : request.eatenAt().toInstant();

        DiaryEntry entry = new DiaryEntry();
        entry.setUserId(userId);
        entry.setProductId(product.getId());
        entry.setMealType(request.mealType());
        entry.setEatenAt(eatenAt);
        entry.setLoggedDate(eatenAt == null ? LocalDate.now(clock) : eatenAt.atZone(clock.getZone()).toLocalDate());
        applySnapshot(entry, product, request.grams());

        return DiaryEntryResponse.from(diaryEntryRepository.save(entry), null);
    }

    @Transactional
    public DiaryEntryResponse updateGrams(Long id, UpdateDiaryEntryRequest request, Long userId) {
        DiaryEntry entry = findOwned(id, userId);
        Optional<Product> product = Optional.ofNullable(entry.getProductId()).flatMap(productRepository::findById);

        if (product.isPresent()) {
            applySnapshot(entry, product.get(), request.grams());
            entry.setProductUpdated(false);
        } else {
            rescaleSnapshot(entry, request.grams());
        }
        return DiaryEntryResponse.from(diaryEntryRepository.save(entry), null);
    }

    @Transactional(readOnly = true)
    public DaySummaryResponse getDay(LocalDate date, Long userId) {
        List<DiaryEntry> entries = diaryEntryRepository.findByUserIdAndLoggedDate(userId, date).stream()
                .sorted(Comparator.comparing(this::effectiveTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(DiaryEntry::getId))
                .toList();
        return DaySummaryResponse.of(date, toResponses(entries));
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats(LocalDate from, LocalDate to, Long userId) {
        if (from.isAfter(to)) {
            throw new BadRequestException("Дата начала периода позже даты окончания");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_STATS_DAYS) {
            throw new BadRequestException("Период не должен превышать " + MAX_STATS_DAYS + " дней");
        }

        Map<LocalDate, DayTotals> totalsByDate = diaryEntryRepository.findDailyTotals(userId, from, to).stream()
                .collect(Collectors.toMap(DayTotals::date, Function.identity()));

        List<StatsResponse.Day> result = from.datesUntil(to.plusDays(1))
                .map(date -> {
                    DayTotals totals = totalsByDate.get(date);
                    return totals == null ? StatsResponse.Day.empty(date) : StatsResponse.Day.of(date, totals);
                })
                .toList();
        return new StatsResponse(from, to, result, average(totalsByDate.values()));
    }

    @Transactional
    public void deleteEntry(Long id, Long userId) {
        diaryEntryRepository.delete(findOwned(id, userId));
    }

    private DiaryEntry findOwned(Long id, Long userId) {
        DiaryEntry entry = diaryEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        if (!entry.getUserId().equals(userId)) {
            throw new ForbiddenException("Запись принадлежит другому пользователю");
        }
        return entry;
    }

    private Instant effectiveTime(DiaryEntry entry) {
        return entry.getEatenAt() != null ? entry.getEatenAt() : entry.getLoggedAt();
    }

    private List<DiaryEntryResponse> toResponses(List<DiaryEntry> entries) {
        Set<Long> staleProductIds = entries.stream()
                .filter(DiaryEntry::isProductUpdated)
                .map(DiaryEntry::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Instant> updatedAtByProductId = staleProductIds.isEmpty()
                ? Map.of()
                : productRepository.findAllById(staleProductIds).stream()
                .filter(product -> product.getUpdatedAt() != null)
                .collect(Collectors.toMap(Product::getId, Product::getUpdatedAt));

        return entries.stream()
                .map(entry -> DiaryEntryResponse.from(
                        entry,
                        entry.isProductUpdated() && entry.getProductId() != null
                                ? updatedAtByProductId.get(entry.getProductId())
                                : null))
                .toList();
    }

    private StatsResponse.Average average(Collection<DayTotals> totals) {
        if (totals.isEmpty()) {
            return null;
        }
        BigDecimal count = BigDecimal.valueOf(totals.size());
        return new StatsResponse.Average(
                average(totals, DayTotals::kcal, count),
                average(totals, DayTotals::protein, count),
                average(totals, DayTotals::fat, count),
                average(totals, DayTotals::carbs, count));
    }

    private BigDecimal average(Collection<DayTotals> totals, Function<DayTotals, BigDecimal> getter, BigDecimal count) {
        return totals.stream()
                .map(getter)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(count, 2, RoundingMode.HALF_UP);
    }

    private void applySnapshot(DiaryEntry entry, Product product, BigDecimal grams) {
        entry.setProductName(product.getName());
        entry.setGrams(grams);
        entry.setKcal(portion(product.getKcalPer100g(), grams));
        entry.setProtein(portion(product.getProteinPer100g(), grams));
        entry.setFat(portion(product.getFatPer100g(), grams));
        entry.setCarbs(portion(product.getCarbsPer100g(), grams));
    }

    private void rescaleSnapshot(DiaryEntry entry, BigDecimal newGrams) {
        BigDecimal oldGrams = entry.getGrams();
        entry.setKcal(rescale(entry.getKcal(), newGrams, oldGrams));
        entry.setProtein(rescale(entry.getProtein(), newGrams, oldGrams));
        entry.setFat(rescale(entry.getFat(), newGrams, oldGrams));
        entry.setCarbs(rescale(entry.getCarbs(), newGrams, oldGrams));
        entry.setGrams(newGrams);
    }

    private BigDecimal portion(BigDecimal per100g, BigDecimal grams) {
        return per100g.multiply(grams).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal rescale(BigDecimal value, BigDecimal newGrams, BigDecimal oldGrams) {
        return value.multiply(newGrams).divide(oldGrams, 2, RoundingMode.HALF_UP);
    }
}
