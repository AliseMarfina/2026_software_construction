package com.calorietracker.backend.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "kcal_per_100g", nullable = false, precision = 7, scale = 2)
    private BigDecimal kcalPer100g;

    @Column(name = "protein_per_100g", nullable = false, precision = 7, scale = 2)
    private BigDecimal proteinPer100g;

    @Column(name = "fat_per_100g", nullable = false, precision = 7, scale = 2)
    private BigDecimal fatPer100g;

    @Column(name = "carbs_per_100g", nullable = false, precision = 7, scale = 2)
    private BigDecimal carbsPer100g;

    @Column(length = 100)
    private String barcode;

    @Column(name = "owner_id")
    private Long ownerId;
}