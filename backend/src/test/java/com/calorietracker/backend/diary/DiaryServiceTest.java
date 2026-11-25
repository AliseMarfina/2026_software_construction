package com.calorietracker.backend.diary;
import com.calorietracker.backend.common.*;
import com.calorietracker.backend.diary.dto.*;
import com.calorietracker.backend.product.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class DiaryServiceTest {
    private final DiaryEntryRepository entries = mock(DiaryEntryRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
    private final DiaryService service = new DiaryService(entries, products, clock);
    private static BigDecimal n(String s) { return new BigDecimal(s); }
    private Product product() { Product p = new Product(); p.setId(2L); p.setName("Oats"); p.setKcalPer100g(n("350")); p.setProteinPer100g(n("12")); p.setFatPer100g(n("7")); p.setCarbsPer100g(n("60")); return p; }
    private DiaryEntry entry() { DiaryEntry e = new DiaryEntry(); e.setId(3L); e.setUserId(1L); e.setProductId(2L); e.setGrams(n("100")); e.setKcal(n("350")); e.setProtein(n("12")); e.setFat(n("7")); e.setCarbs(n("60")); return e; }
    @Test void addsEntryAndCalculatesPortion() {
        when(products.findVisibleById(2L,1L)).thenReturn(Optional.of(product()));
        when(entries.save(any(DiaryEntry.class))).thenAnswer(i -> i.getArgument(0));
        var result = service.addEntry(new CreateDiaryEntryRequest(2L,n("150"),"breakfast",null),1L);
        assertEquals(n("525.00"),result.kcal()); assertEquals(n("18.00"),result.protein()); assertEquals(n("10.50"),result.fat()); assertEquals(n("90.00"),result.carbs());
        verify(entries).save(argThat(e -> LocalDate.of(2026,6,1).equals(e.getLoggedDate()) && Long.valueOf(1).equals(e.getUserId())));
    }
    @Test void rejectsMissingProduct() { when(products.findVisibleById(2L,1L)).thenReturn(Optional.empty()); assertThrows(NotFoundException.class, () -> service.addEntry(new CreateDiaryEntryRequest(2L,n("100"),"lunch",null),1L)); verifyNoInteractions(entries); }
    @Test void updatesFromCurrentProduct() { DiaryEntry e=entry(); e.setProductUpdated(true); when(entries.findById(3L)).thenReturn(Optional.of(e)); when(products.findById(2L)).thenReturn(Optional.of(product())); when(entries.save(e)).thenReturn(e); var result=service.updateGrams(3L,new UpdateDiaryEntryRequest(n("50")),1L); assertEquals(n("175.00"),result.kcal()); assertFalse(result.productUpdated()); }
    @Test void rescalesSnapshotWhenProductMissing() { DiaryEntry e=entry(); when(entries.findById(3L)).thenReturn(Optional.of(e)); when(products.findById(2L)).thenReturn(Optional.empty()); when(entries.save(e)).thenReturn(e); var result=service.updateGrams(3L,new UpdateDiaryEntryRequest(n("50")),1L); assertEquals(n("175.00"),result.kcal()); assertEquals(n("6.00"),result.protein()); }
    @Test void deletesOwnedEntry() { DiaryEntry e=entry(); when(entries.findById(3L)).thenReturn(Optional.of(e)); service.deleteEntry(3L,1L); verify(entries).delete(e); }
    @Test void forbidsDeletingOtherUsersEntry() { when(entries.findById(3L)).thenReturn(Optional.of(entry())); assertThrows(ForbiddenException.class,()->service.deleteEntry(3L,99L)); verify(entries,never()).delete(any()); }
    @Test void rejectsMissingEntry() { when(entries.findById(3L)).thenReturn(Optional.empty()); assertThrows(NotFoundException.class,()->service.deleteEntry(3L,1L)); }
    @Test void rejectsReversedStatsPeriod() { LocalDate d=LocalDate.of(2026,6,1); assertThrows(BadRequestException.class,()->service.getStats(d,d.minusDays(1),1L)); verifyNoInteractions(entries); }
    @Test void rejectsTooLongStatsPeriod() { LocalDate d=LocalDate.of(2026,6,1); assertThrows(BadRequestException.class,()->service.getStats(d,d.plusDays(366),1L)); verifyNoInteractions(entries); }
    @Test void statsIncludeMissingDaysAndAverageOnlyRecordedDays() { LocalDate d=LocalDate.of(2026,6,1); when(entries.findDailyTotals(1L,d,d.plusDays(2))).thenReturn(List.of(new DayTotals(d,n("100"),n("10"),n("2"),n("15")),new DayTotals(d.plusDays(2),n("300"),n("20"),n("4"),n("25")))); var result=service.getStats(d,d.plusDays(2),1L); assertEquals(3,result.days().size()); assertTrue(result.days().get(1).noData()); assertEquals(n("200.00"),result.average().kcal()); }
    @Test void emptyStatsHaveNoAverage() { LocalDate d=LocalDate.of(2026,6,1); when(entries.findDailyTotals(1L,d,d)).thenReturn(List.of()); var result=service.getStats(d,d,1L); assertTrue(result.days().get(0).noData()); assertNull(result.average()); }
}
