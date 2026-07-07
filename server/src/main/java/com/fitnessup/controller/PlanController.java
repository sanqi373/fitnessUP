package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.TrainingPlan;
import com.fitnessup.service.TrainingPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/train")
public class PlanController {

    private final TrainingPlanService trainingPlanService;

    public PlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResult<?>> getPlans() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TrainingPlan> plans = trainingPlanService.getPlansByUserId(userId);
        return ResponseEntity.ok(ApiResult.success(plans));
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<ApiResult<?>> getPlanById(@PathVariable Long id) {
        Optional<TrainingPlan> plan = trainingPlanService.getPlanById(id);
        if (plan.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "训练计划不存在"));
        }
        return ResponseEntity.ok(ApiResult.success(plan.get()));
    }

    @PostMapping("/plans")
    public ResponseEntity<ApiResult<?>> createPlan(@RequestBody TrainingPlan plan) {
        if (plan.getStatus() == null) {
            plan.setStatus("draft");
        }
        TrainingPlan created = trainingPlanService.createPlan(plan);
        return ResponseEntity.ok(ApiResult.success(created));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<ApiResult<?>> updatePlan(@PathVariable Long id, @RequestBody TrainingPlan plan) {
        plan.setId(id);
        TrainingPlan updated = trainingPlanService.updatePlan(plan);
        return ResponseEntity.ok(ApiResult.success(updated));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<ApiResult<?>> deletePlan(@PathVariable Long id) {
        trainingPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResult.success("删除成功", null));
    }
}
