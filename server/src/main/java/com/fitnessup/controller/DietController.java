package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.DietRecommendation;
import com.fitnessup.service.DietRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diet")
public class DietController {

    private final DietRecommendationService dietRecommendationService;

    public DietController(DietRecommendationService dietRecommendationService) {
        this.dietRecommendationService = dietRecommendationService;
    }

    @GetMapping("/recommend")
    public ResponseEntity<ApiResult<?>> getRecommendations() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DietRecommendation> recommendations = dietRecommendationService.getRecommendationsByUserId(userId);
        return ResponseEntity.ok(ApiResult.success(recommendations));
    }
}
