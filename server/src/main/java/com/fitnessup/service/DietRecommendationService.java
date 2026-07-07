package com.fitnessup.service;

import com.fitnessup.entity.DietRecommendation;
import com.fitnessup.repository.DietRecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DietRecommendationService {

    private final DietRecommendationRepository dietRecommendationRepository;

    public DietRecommendationService(DietRecommendationRepository dietRecommendationRepository) {
        this.dietRecommendationRepository = dietRecommendationRepository;
    }

    public List<DietRecommendation> getRecommendationsByUserId(Long userId) {
        return dietRecommendationRepository.findByUserId(userId);
    }
}
