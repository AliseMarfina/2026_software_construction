package com.calorietracker.backend.product.off;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
class OpenFoodFactsClientTest {
    private final OpenFoodFactsClient client=new OpenFoodFactsClient(new OffProperties("http://localhost:1","unit-test",20,Duration.ofSeconds(1),2,10));
    @Test void blankQueryReturnsEmptyWithoutNetwork() { assertTrue(client.search("   ").isEmpty()); }
    @Test void punctuationOnlyQueryReturnsEmptyWithoutNetwork() { assertTrue(client.search("+!?/()[]").isEmpty()); }
}
