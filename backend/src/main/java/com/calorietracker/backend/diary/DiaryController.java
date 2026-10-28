package com.calorietracker.backend.diary;

import com.calorietracker.backend.auth.CurrentUser;
import com.calorietracker.backend.diary.dto.CreateDiaryEntryRequest;
import com.calorietracker.backend.diary.dto.DaySummaryResponse;
import com.calorietracker.backend.diary.dto.DiaryEntryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryResponse addEntry(@Valid @RequestBody CreateDiaryEntryRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return diaryService.addEntry(request, CurrentUser.id(jwt));
    }

    @GetMapping("/day")
    public DaySummaryResponse getDay(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                     @AuthenticationPrincipal Jwt jwt) {
        return diaryService.getDay(date, CurrentUser.id(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        diaryService.deleteEntry(id, CurrentUser.id(jwt));
    }
}