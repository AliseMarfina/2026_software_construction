package com.calorietracker.backend.product;
import com.calorietracker.backend.diary.DiaryEntryRepository;
import com.calorietracker.backend.product.off.OffProduct;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
class ProductImporterTest {
    private final ProductRepository repo=mock(ProductRepository.class);
    private final DiaryEntryRepository diary=mock(DiaryEntryRepository.class);
    private final ProductImporter importer=new ProductImporter(repo,diary);
    private final BigDecimal n=BigDecimal.ONE;
    private final OffProduct product=new OffProduct("123","Milk",n,n,n,n);
    @Test void changedNutrientsMarkDiaryEntries() { when(repo.updateNutrientsIfChanged("123",n,n,n,n)).thenReturn(1); importer.importProduct(product); verify(diary).markProductUpdated("123"); verify(repo).insertIfAbsent("Milk",n,n,n,n,"123"); }
    @Test void unchangedNutrientsDoNotMarkDiaryEntries() { when(repo.updateNutrientsIfChanged("123",n,n,n,n)).thenReturn(0); importer.importProduct(product); verifyNoInteractions(diary); verify(repo).insertIfAbsent("Milk",n,n,n,n,"123"); }
}
