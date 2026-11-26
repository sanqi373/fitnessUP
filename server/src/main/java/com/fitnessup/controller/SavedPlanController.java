package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.repository.DietRecommendationRepository;
import com.fitnessup.repository.TrainingPlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/plans")
public class SavedPlanController {
    private final TrainingPlanRepository training;
    private final DietRecommendationRepository diet;

    public SavedPlanController(TrainingPlanRepository training, DietRecommendationRepository diet) {
        this.training = training;
        this.diet = diet;
    }

    @GetMapping("/training")
    public ApiResult<?> listTraining(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(training.findSaved(userId));
    }

    @GetMapping("/training/{id}")
    public ApiResult<Boolean> hasTraining(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ApiResult.success(training.countSaved(userId, id) > 0);
    }

    @PutMapping("/training/{id}")
    @Transactional
    public ResponseEntity<ApiResult<?>> addTraining(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        training.addSaved(userId, id);
        if (training.countSaved(userId, id) == 0) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "训练不存在"));
        }
        return ResponseEntity.ok(ApiResult.success(true));
    }

    @DeleteMapping("/training/{id}")
    public ApiResult<Boolean> removeTraining(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        training.removeSaved(userId, id);
        return ApiResult.success(false);
    }

    @GetMapping("/diet")
    public ApiResult<?> listDiet(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(diet.findSaved(userId));
    }

    @GetMapping("/diet/{id}")
    public ApiResult<Boolean> hasDiet(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ApiResult.success(diet.countSaved(userId, id) > 0);
    }

    @PutMapping("/diet/{id}")
    @Transactional
    public ResponseEntity<ApiResult<?>> addDiet(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        diet.addSaved(userId, id);
        if (diet.countSaved(userId, id) == 0) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "食谱不存在"));
        }
        return ResponseEntity.ok(ApiResult.success(true));
    }

    @DeleteMapping("/diet/{id}")
    public ApiResult<Boolean> removeDiet(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        diet.removeSaved(userId, id);
        return ApiResult.success(false);
    }
}
