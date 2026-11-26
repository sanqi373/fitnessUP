package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.TrainingPlan;
import com.fitnessup.service.TrainingPlanService;
import com.fitnessup.service.WorkoutService;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/train")
public class PlanController {
    private final TrainingPlanService trainingPlanService;
    private final WorkoutService workouts;

    public PlanController(TrainingPlanService trainingPlanService, WorkoutService workouts) {
        this.trainingPlanService = trainingPlanService;
        this.workouts = workouts;
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResult<?>> getPlans(@AuthenticationPrincipal Long userId) {
        List<TrainingPlan> plans = trainingPlanService.getVisiblePlans(userId);
        return ResponseEntity.ok(ApiResult.success(Map.of("list", plans, "total", plans.size())));
    }

    public record Detail(@JsonUnwrapped TrainingPlan plan, WorkoutService.Progress progress) {}

    @GetMapping("/plans/{id}")
    public ResponseEntity<ApiResult<?>> getPlanById(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        Optional<TrainingPlan> plan = trainingPlanService.getPlanById(id, userId);
        if (plan.isEmpty()) return ResponseEntity.status(404).body(ApiResult.error(404, "训练计划不存在"));
        return ResponseEntity.ok(ApiResult.success(new Detail(plan.get(), workouts.progress(userId, id))));
    }

    public record TimerCommand(@NotNull UUID sessionId, @NotNull WorkoutService.Action action) {}

    @PostMapping("/plans/{id}/timer")
    public ApiResult<?> changeTimer(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                                    @Valid @RequestBody TimerCommand command) {
        return ApiResult.success(workouts.change(userId, id, command.sessionId(), command.action()));
    }

    @PostMapping("/plans")
    public ResponseEntity<ApiResult<?>> createPlan(@RequestBody TrainingPlan plan, @AuthenticationPrincipal Long userId) {
        plan.setId(null);
        plan.setUserId(userId);
        plan.setCatalogItem(false);
        if (plan.getStatus() == null) plan.setStatus("draft");
        return ResponseEntity.ok(ApiResult.success(trainingPlanService.createPlan(plan)));
    }

    @PutMapping("/plans/{id}")
    @Transactional
    public ResponseEntity<ApiResult<?>> updatePlan(@PathVariable Long id, @RequestBody TrainingPlan plan, @AuthenticationPrincipal Long userId) {
        var existing = trainingPlanService.getPlanById(id, userId);
        if (existing.isEmpty() || existing.get().isCatalogItem() || !userId.equals(existing.get().getUserId())) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "训练计划不存在"));
        }
        plan.setId(id);
        plan.setUserId(userId);
        plan.setCatalogItem(false);
        return ResponseEntity.ok(ApiResult.success(trainingPlanService.updatePlan(plan)));
    }

    @DeleteMapping("/plans/{id}")
    @Transactional
    public ResponseEntity<ApiResult<?>> deletePlan(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        var existing = trainingPlanService.getPlanById(id, userId);
        if (existing.isEmpty() || existing.get().isCatalogItem() || !userId.equals(existing.get().getUserId())) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "训练计划不存在"));
        }
        trainingPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResult.success("删除成功", null));
    }
}
