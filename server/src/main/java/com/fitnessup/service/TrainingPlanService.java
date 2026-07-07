package com.fitnessup.service;

import com.fitnessup.entity.TrainingPlan;
import com.fitnessup.repository.TrainingPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;

    public TrainingPlanService(TrainingPlanRepository trainingPlanRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
    }

    public List<TrainingPlan> getPlansByUserId(Long userId) {
        return trainingPlanRepository.findByUserId(userId);
    }

    public Optional<TrainingPlan> getPlanById(Long id) {
        return trainingPlanRepository.findById(id);
    }

    public TrainingPlan createPlan(TrainingPlan plan) {
        return trainingPlanRepository.save(plan);
    }

    public TrainingPlan updatePlan(TrainingPlan plan) {
        return trainingPlanRepository.save(plan);
    }

    public void deletePlan(Long id) {
        trainingPlanRepository.deleteById(id);
    }
}
