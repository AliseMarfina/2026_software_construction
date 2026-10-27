package com.calorietracker.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
            SELECT * FROM products
            WHERE name ILIKE '%' || :query || '%'
              AND (owner_id IS NULL OR owner_id = :userId)
            ORDER BY name
            LIMIT :limit
            """, nativeQuery = true)
    List<Product> search(@Param("query") String query,
                         @Param("userId") Long userId,
                         @Param("limit") int limit);

    List<Product> findByBarcodeIn(Collection<String> barcodes);

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO products (name, kcal_per_100g, protein_per_100g, fat_per_100g, carbs_per_100g, barcode)
        VALUES (:name, :kcal, :protein, :fat, :carbs, :barcode)
        ON CONFLICT (barcode) DO NOTHING
        """, nativeQuery = true)
    void insertIfAbsent(@Param("name") String name,
                        @Param("kcal") BigDecimal kcal,
                        @Param("protein") BigDecimal protein,
                        @Param("fat") BigDecimal fat,
                        @Param("carbs") BigDecimal carbs,
                        @Param("barcode") String barcode);

    @Query("select p from Product p where p.id = :id and (p.ownerId is null or p.ownerId = :userId)")
    Optional<Product> findVisibleById(@Param("id") Long id, @Param("userId") Long userId);
}