package com.fitnessup.service;

import com.fitnessup.entity.DietRecommendation;
import com.fitnessup.repository.DietRecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DietRecommendationService {
    private final DietRecommendationRepository repository;

    public DietRecommendationService(DietRecommendationRepository repository) {
        this.repository = repository;
    }

    public List<DietRecommendation> getRecommendations(Long userId) {
        return repository.findVisible(userId);
    }

    public Optional<DietRecommendation> getRecommendation(Long id, Long userId) {
        return repository.findVisibleById(id, userId);
    }
}
