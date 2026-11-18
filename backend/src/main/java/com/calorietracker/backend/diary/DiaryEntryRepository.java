package com.calorietracker.backend.diary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    List<DiaryEntry> findByUserIdAndLoggedDate(Long userId, LocalDate loggedDate);

    @Query("""
            select new com.calorietracker.backend.diary.DayTotals(
                e.loggedDate, sum(e.kcal), sum(e.protein), sum(e.fat), sum(e.carbs))
            from DiaryEntry e
            where e.userId = :userId and e.loggedDate between :periodStart and :periodEnd
            group by e.loggedDate
            order by e.loggedDate
            """)
    List<DayTotals> findDailyTotals(@Param("userId") Long userId,
                                    @Param("periodStart") LocalDate periodStart,
                                    @Param("periodEnd") LocalDate periodEnd);

    @Transactional
    @Modifying
    @Query("""
            update DiaryEntry e set e.productUpdated = true
            where e.productId in (select p.id from Product p where p.barcode = :barcode)
            """)
    int markProductUpdated(@Param("barcode") String barcode);
}
