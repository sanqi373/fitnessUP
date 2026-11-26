package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.DietRecommendation;
import com.fitnessup.service.DietRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diet")
public class DietController {
    private final DietRecommendationService service;

    public DietController(DietRecommendationService service) {
        this.service = service;
    }

    @GetMapping("/recommend")
    public ApiResult<List<DietRecommendation>> getRecommendations(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(service.getRecommendations(userId));
    }

    @GetMapping("/recommend/{id}")
    public ResponseEntity<ApiResult<?>> getRecommendation(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        var recipe = service.getRecommendation(id, userId);
        if (recipe.isEmpty()) return ResponseEntity.status(404).body(ApiResult.error(404, "食谱不存在"));
        return ResponseEntity.ok(ApiResult.success(recipe.get()));
    }
}
