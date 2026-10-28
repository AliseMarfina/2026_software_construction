package com.calorietracker.backend.diary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "diary_entries")
@Getter
@Setter
@NoArgsConstructor
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal grams;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal kcal;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal protein;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal fat;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal carbs;

    @CreationTimestamp
    @Column(name = "logged_at", updatable = false)
    private Instant loggedAt;

    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;
}