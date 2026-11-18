package com.calorietracker.backend.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    List<DiaryEntry> findByUserIdAndLoggedDateOrderByIdAsc(Long userId, LocalDate loggedDate);
}