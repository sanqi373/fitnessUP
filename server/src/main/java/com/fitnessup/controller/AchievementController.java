package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.UserAchievement;
import com.fitnessup.service.UserAchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class AchievementController {

    private final UserAchievementService userAchievementService;

    public AchievementController(UserAchievementService userAchievementService) {
        this.userAchievementService = userAchievementService;
    }

    @GetMapping("/achievements")
    public ResponseEntity<ApiResult<?>> getAchievements() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserAchievement> achievements = userAchievementService.getAchievementsByUserId(userId);
        return ResponseEntity.ok(ApiResult.success(achievements));
    }
}
