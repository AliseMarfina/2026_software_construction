package com.calorietracker.backend.product;
import com.calorietracker.backend.common.*;
import com.calorietracker.backend.product.dto.*;
import com.calorietracker.backend.product.off.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProductServiceTest {
    private final ProductRepository repo=mock(ProductRepository.class);
    private final ProductImporter importer=mock(ProductImporter.class);
    private final OpenFoodFactsClient client=mock(OpenFoodFactsClient.class);
    private final SlidingWindowRateLimiter limiter=mock(SlidingWindowRateLimiter.class);
    private final ProductService service=new ProductService(repo,importer,client,limiter,new OffProperties("http://localhost","test",20,Duration.ofSeconds(1),2,10));
    private Product product(long id,String name) { Product p=new Product(); p.setId(id); p.setName(name); return p; }
    @Test void rejectsShortQuery() { assertThrows(BadRequestException.class,()->service.search(" a ",1L)); verifyNoInteractions(repo,client); }
    @Test void sufficientLocalResultsAvoidExternalApi() { when(repo.search("milk",1L,20)).thenReturn(List.of(product(1,"Milk"),product(2,"Milk 2"))); assertEquals(2,service.search(" milk ",1L).size()); verifyNoInteractions(client,limiter); }
    @Test void escapesSqlLikeWildcards() { when(repo.search("a\\%\\_",1L,20)).thenReturn(List.of(product(1,"A"),product(2,"B"))); assertEquals(2,service.search("a%_",1L).size()); }
    @Test void rateLimitFallsBackToLocalResults() { when(repo.search("milk",1L,20)).thenReturn(List.of(product(1,"Milk"))); when(limiter.tryAcquire()).thenReturn(false); assertEquals(1,service.search("milk",1L).size()); verifyNoInteractions(client); }
    @Test void rateLimitWithoutLocalResultsThrows() { when(repo.search("milk",1L,20)).thenReturn(List.of()); when(limiter.tryAcquire()).thenReturn(false); assertThrows(ExternalServiceUnavailableException.class,()->service.search("milk",1L)); }
    @Test void networkFailureFallsBackToLocalResults() { when(repo.search("milk",1L,20)).thenReturn(List.of(product(1,"Milk"))); when(limiter.tryAcquire()).thenReturn(true); when(client.search("milk")).thenThrow(new RestClientException("offline")); assertEquals(1,service.search("milk",1L).size()); }
    @Test void emptySearchThrowsNotFound() { when(repo.search("milk",1L,20)).thenReturn(List.of()); when(limiter.tryAcquire()).thenReturn(true); when(client.search("milk")).thenReturn(List.of()); assertThrows(NotFoundException.class,()->service.search("milk",1L)); }
    @Test void mergesImportedResultsWithoutDuplicates() { Product a=product(1,"Z milk"), b=product(2,"A milk"); var off=new OffProduct("123","A milk",BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE); when(repo.search("milk",1L,20)).thenReturn(List.of(a)); when(limiter.tryAcquire()).thenReturn(true); when(client.search("milk")).thenReturn(List.of(off)); when(repo.findByBarcodeIn(List.of("123"))).thenReturn(List.of(a,b)); var result=service.search("milk",1L); assertEquals(List.of(2L,1L),result.stream().map(ProductResponse::id).toList()); verify(importer).importProduct(off); }
    @Test void createsCustomProductForOwner() { when(repo.save(any(Product.class))).thenAnswer(i->i.getArgument(0)); var result=service.createCustom(new CreateCustomProductRequest(" Oats ",BigDecimal.TEN,BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE),1L); assertEquals("Oats",result.name()); assertEquals("custom",result.source()); verify(repo).save(argThat(p->Long.valueOf(1).equals(p.getOwnerId()))); }
}
