package com.calorietracker.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}